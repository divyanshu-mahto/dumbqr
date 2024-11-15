package com.dumbqr.dumbqr.controller;

import com.dumbqr.dumbqr.model.QrScanLog;
import com.dumbqr.dumbqr.model.User;
import com.dumbqr.dumbqr.repository.QrScanLogRepository;
import com.dumbqr.dumbqr.service.GeolocationService;
import com.dumbqr.dumbqr.service.JwtService;
import com.google.zxing.WriterException;
import com.dumbqr.dumbqr.model.QrCode;
import com.dumbqr.dumbqr.repository.UserRepository;
import com.dumbqr.dumbqr.service.QrCodeService;
import com.dumbqr.dumbqr.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
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


    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user){

        try {
            userService.register(user);
            return new ResponseEntity<>(userService.register(user),HttpStatus.OK);

        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user){
        try {
            return new ResponseEntity<>(userService.verify(user),HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }
    }


    @GetMapping("/greet")
    public String greet(){
        return "Hello There!";
    }

    //display a qr code
    @GetMapping("/qr/{shortId}")
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
    public ResponseEntity<?> createNewQr(@RequestHeader("Authorization") String authHeader, @RequestBody QrCode qrCode) throws IOException, WriterException {
        //create a short url
        //it should be unique
        //either generate random uid or take some user input and check if it exists in db or not <-- do this
        String shorturl = qrCode.getShortUrl();

        if (!qrCodeService.checkShortId(shorturl).getStatusCode().equals(HttpStatus.OK)){
            return new ResponseEntity<>("Short url already ", HttpStatus.NOT_ACCEPTABLE);
        }
        //set short url of qr
        qrCode.setShortUrl(shorturl);

        //add the qr
        qrCodeService.addQr(qrCode);

        //save qr in database  this is redundant qr code is already being inserted in above method
        qrCodeService.createQR(qrCode);

        //get the user email
        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);

        //find the user in db
        User user = userRepository.findByEmail(email);

        //add the new qr code id
        List<ObjectId> qrcodes = user.getQrcodes();
        qrcodes.add(qrCode.getId());
        user.setQrcodes(qrcodes);
        //update in db
        userService.updateQrCodeList(user);

        return new ResponseEntity<>("QR code created",HttpStatus.CREATED);
    }

    //Redirect link noAuth
    @GetMapping("goto/{shortId}")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<?> redirect(@PathVariable String shortId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            //check if the short id exists
            if(qrCodeService.verifyShortId(shortId).getStatusCode().equals(HttpStatus.NOT_FOUND)){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            String location = geolocationService.getGeolocation(request.getRemoteAddr()).getBody();

            QrScanLog qrScanLog = new QrScanLog();
            qrScanLog.setShortId(shortId);
            qrScanLog.setTimestamp(LocalDateTime.now());
            qrScanLog.setIp(request.getRemoteAddr());
            qrScanLog.setUserAgent(request.getHeader("User-Agent"));
            qrScanLog.setLocation(location);

            qrScanLogRepository.save(qrScanLog);

            QrCode qrCode = qrCodeService.getQrCodeObject(shortId);
            //add the object id of scan in qr list of scans
            qrCodeService.addQrScan(qrCode,qrScanLog);

            String redirectUrl = qrCodeService.getRedirectUrl(shortId);

            response.sendRedirect(redirectUrl);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    //view list of all QR code
    @GetMapping("/allqr")
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
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //update the redirect url
    @PostMapping("/qr/update/{shortId}")
    public ResponseEntity<?> updateRedirectUrl(@RequestHeader("Authorization") String authHeader, @PathVariable String shortId, @RequestBody String newRedirectUrl){
        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            return qrCodeService.updateQrCode(user, shortId, newRedirectUrl);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //update user password
    @PostMapping("/user/update")
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
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Delete user
    @DeleteMapping("/user/delete")
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
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //all scans of a QR code
    @GetMapping("/qr/analytics/{shortId}")
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
            if(user.getQrcodes().contains(qrCode.getId())){
                return qrCodeService.getAnalytics(qrCode);
            }else {
                return new ResponseEntity<>("QR code not found",HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
