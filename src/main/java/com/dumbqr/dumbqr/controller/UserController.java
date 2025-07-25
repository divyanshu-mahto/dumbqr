package com.dumbqr.dumbqr.controller;

import com.dumbqr.dumbqr.dto.VerifyForgotPasswordDto;
import com.dumbqr.dumbqr.dto.VerifyUserEmailDto;
import com.dumbqr.dumbqr.model.User;
import com.dumbqr.dumbqr.repository.QrScanLogRepository;
import com.dumbqr.dumbqr.service.*;
import com.google.zxing.WriterException;
import com.dumbqr.dumbqr.model.QrCode;
import com.dumbqr.dumbqr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GeolocationService geolocationService;

    @Autowired
    private QrScanLogRepository qrScanLogRepository;

    @Autowired
    private BloomFilterService bloomFilterService;

    private static final List<String> RESERVED_PREFIXES = List.of("api", "admin", "dashboard");


    @PostMapping("/signup")
    @RateLimited
    public ResponseEntity<?> registerUser(@RequestBody User user){
        if(user.getEmail() == null || user.getPassword() == null || user.getEmail().length() == 0 || user.getPassword().length() == 0){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            userService.register(user);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    @RateLimited
    public ResponseEntity<?> login(@RequestBody User user){
        if(user.getEmail() == null || user.getPassword() == null || user.getEmail().length() == 0 || user.getPassword().length() == 0){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            String token = userService.verifyLogin(user);
            if(token.equals("Fail")){
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(Map.of("username", user.getEmail().split("@")[0], "token", token),HttpStatus.OK);
        }catch (Exception e){
            if(e.getMessage().equals("Account not verified")) return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
            else if(e.getMessage().equals("Error sending mail")) return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            else return new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/verify")
    @RateLimited
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserEmailDto verifyUserEmailDto){
        try{
            if(verifyUserEmailDto.getEmail() == null || verifyUserEmailDto.getCode() == null || verifyUserEmailDto.getEmail().length() == 0 || verifyUserEmailDto.getCode().length() == 0){
                return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            }
            User user = userRepository.findByEmail(verifyUserEmailDto.getEmail());
            if(user == null){
                return new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);
            }
            //if verification successful return token
            String token = userService.verifyUser(user, verifyUserEmailDto.getCode());
            return new ResponseEntity<>(Map.of("username", user.getEmail().split("@")[0], "token", token), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/resend")
    @RateLimited
    public ResponseEntity<String> resendEmail(@RequestBody String email){
        try{
            User user = userRepository.findByEmail(email);
            if(user == null){
                return new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);
            }
            userService.resendVerificationEmail(user);
            return new ResponseEntity<>("Verification code sent", HttpStatus.OK);
        } catch (Exception e){
            if(e.getMessage().equals("User already verified")) return new ResponseEntity<>(e.getMessage(), HttpStatus.ACCEPTED);
            else if(e.getMessage().equals("User not found")) return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            else return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/forgot")
    @RateLimited
    public ResponseEntity<String> forgotPasswordRequest(@RequestBody String email){
        if(email == null || email.length() == 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        try{
            User user = userRepository.findByEmail(email);
            if(user == null){
                return new ResponseEntity<>("No user found for the provided email", HttpStatus.NOT_FOUND);
            }
            userService.forgotPasswordMail(user);
            return new ResponseEntity<>("Password reset verification code sent", HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/forgot/changepassword")
    @RateLimited
    public ResponseEntity<?> forgotChangePassword(@RequestBody VerifyForgotPasswordDto verifyForgotPasswordDto){
        if(verifyForgotPasswordDto.getEmail() == null || verifyForgotPasswordDto.getCode() == null || verifyForgotPasswordDto.getNewPassword() == null || verifyForgotPasswordDto.getEmail().length() == 0 || verifyForgotPasswordDto.getCode().length() == 0 || verifyForgotPasswordDto.getNewPassword().length() == 0){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try{
            User user = userRepository.findByEmail(verifyForgotPasswordDto.getEmail());
            if(user == null){
                return new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);
            }
            //if verification successful return token
            String token = userService.verifyForgotPassword(user, verifyForgotPasswordDto);

            return new ResponseEntity<>(Map.of("username", user.getEmail().split("@")[0], "token", token), HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    //display a qr code
    @GetMapping("/qr/{shortId}")
    @RateLimited
    public ResponseEntity<byte[]> getQrCode(@RequestHeader("Authorization") String authHeader, @PathVariable String shortId) {

        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            return qrCodeService.getQr(user, shortId);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //To create a new qr
    @PostMapping("/createqr")
    @RateLimited
    public ResponseEntity<?> createNewQr(@RequestHeader("Authorization") String authHeader, @RequestBody QrCode qrCode) throws IOException, WriterException {
        if(qrCode.getShortId().equals("") || qrCode.getRedirectUrl().equals("") || qrCode.getName().equals("")){
            return new ResponseEntity<>("ShortURL or RedirectURL empty",HttpStatus.BAD_REQUEST);
        }
        if(RESERVED_PREFIXES.stream().anyMatch(qrCode.getShortId()::startsWith)){
            return new ResponseEntity<>("ShortURL contains reserved keywords", HttpStatus.BAD_REQUEST);
        }

        //bloom filter
        if(bloomFilterService.lookUp(qrCode.getShortId())){
            return new ResponseEntity<>("Short url already taken", HttpStatus.NOT_ACCEPTABLE);
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);

        User user = userRepository.findByEmail(email);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.UNAUTHORIZED);
        }

        try{
            qrCode.setUser(user);
            user.getQrCodes().add(qrCode);
            qrCodeService.createQr(qrCode);
            return new ResponseEntity<>("Qr code successfully created with Id:"+qrCode.getId(), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //view list of all QR code
    @GetMapping("/allqr")
    @RateLimited
    public ResponseEntity<List<QrCode>> viewAllQr(@RequestHeader("Authorization") String authHeader){
        try{
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if(user == null){
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            return userService.getAllQr(user);

        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    //delete a qr
    @DeleteMapping("/qr/{shortId}")
    @RateLimited
    public ResponseEntity<?> deleteQr(@RequestHeader("Authorization") String authHeader, @PathVariable String shortId){
        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            return qrCodeService.deleteQrCode(user, shortId);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //update the redirect url and name
    @PostMapping("/qr/update/{shortId}")
    @RateLimited
    public ResponseEntity<?> updateRedirectUrl(@RequestHeader("Authorization") String authHeader, @PathVariable String shortId, @RequestBody QrCode newQrCode){
        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            return qrCodeService.updateQrCode(user, shortId, newQrCode.getRedirectUrl(), newQrCode.getName());

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //update user password
    @PostMapping("/user/update")
    @RateLimited
    public ResponseEntity<?> updateUserPassword(@RequestHeader("Authorization") String authHeader, @RequestBody User newuser){
        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            user.setPassword(newuser.getPassword());
            return userService.updateUser(user);


        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Delete user
    @DeleteMapping("/user/delete")
    @RateLimited
    public ResponseEntity<?> updateUserPassword(@RequestHeader("Authorization") String authHeader){
        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            return userService.deleteUser(user);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //all scans of a QR code
    @GetMapping("/qr/analytics/{shortId}")
    @RateLimited
    public ResponseEntity<?> qrAnalytics(@RequestHeader("Authorization") String authHeader, @PathVariable String shortId){
        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            QrCode qrCode = qrCodeService.getQrCodeObject(shortId);

            //check if the QR code belongs to the user
            if(user.getQrCodes().contains(qrCode)){
                return qrCodeService.getAnalytics(qrCode);
            }else {
                return new ResponseEntity<>("QR code not found",HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
