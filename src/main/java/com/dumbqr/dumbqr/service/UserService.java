package com.dumbqr.dumbqr.service;

import com.dumbqr.dumbqr.dto.VerifyForgotPasswordDto;
import com.dumbqr.dumbqr.model.QrCode;
import com.dumbqr.dumbqr.model.User;
import com.dumbqr.dumbqr.repository.QrCodeRepository;
import com.dumbqr.dumbqr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EmailService emailService;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(10);


    public User register(User user){
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DataIntegrityViolationException("Email already in use");
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5L));
        user.setVerified(false);
        String emailSentResponse = emailService.sendVerificationEmail(user);
        if(!emailSentResponse.equals("Success")){
            throw new RuntimeException("Error sending mail");
        }
        return userRepository.save(user);
    }

    public String verifyUser(User user, String userCode){
        if(user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())){
            resendVerificationEmail(user);
            throw new RuntimeException("Code expired");
        }
        if(user.getVerificationCode().equals(userCode)){
            user.setVerified(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepository.save(user);
            //return token
            return jwtService.generateToken(user.getEmail());
        } else{
            throw new RuntimeException("Invalid code");
        }
    }

    public String generateVerificationCode(){
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    public void resendVerificationEmail(User user){
        if(user.isVerified()){
            throw new RuntimeException("User already verified");
        }
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        String emailSentResponse = emailService.sendVerificationEmail(user);
        if(!emailSentResponse.equals("Success")){
            throw new RuntimeException("Error sending mail");
        }
        userRepository.save(user);
    }

    public void resendForgotPasswordEmail(User user){
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        String sendEmailResponse =  emailService.sendForgotPasswordEmail(user);
        if(!sendEmailResponse.equals("Success")){
            throw new RuntimeException("Error sending mail");
        }
        userRepository.save(user);
    }

    public String verifyLogin(User user){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        if (authentication.isAuthenticated()){
            User user1 = userRepository.findByEmail(user.getEmail());
            if(!user1.isVerified()){
                resendVerificationEmail(user1);
                throw new RuntimeException("Account not verified");
            }
            return jwtService.generateToken(user.getEmail());
        }else{
            return "Fail";
        }
    }

    public void forgotPasswordMail(User user){
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        resendForgotPasswordEmail(user);
    }

    public String verifyForgotPassword(User user, VerifyForgotPasswordDto verifyForgotPasswordDto){
        if(user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())){
            resendForgotPasswordEmail(user);
            throw new RuntimeException("Code expired");
        }
        if(user.getVerificationCode().equals(verifyForgotPasswordDto.getCode())){
            user.setPassword(bCryptPasswordEncoder.encode(verifyForgotPasswordDto.getNewPassword()));
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepository.save(user);
            return jwtService.generateToken(user.getEmail());
        } else{
            throw new RuntimeException("Invalid code");
        }
    }

    public ResponseEntity<List<QrCode>> getAllQr(User user) {

        List<QrCode> qrCodes = user.getQrCodes();

        qrCodes.forEach(qrCode -> {
            // Check if the QR code shortId exists in Redis
            if (Boolean.FALSE.equals(redisTemplate.hasKey(qrCode.getShortId()))) {
                // Cache if not already cached
                redisTemplate.opsForValue().set(qrCode.getShortId(), qrCode.getRedirectUrl(), Duration.ofMinutes(10));
            }
        });

        return new ResponseEntity<>(qrCodes, HttpStatus.OK);
    }


    public ResponseEntity<?> updateUser(User user){
        userRepository.save(user);
        return new ResponseEntity<>("User details updated",HttpStatus.OK);
    }

    public ResponseEntity<?> deleteUser(User user){
        userRepository.delete(user);
        return new ResponseEntity<>("User deleted",HttpStatus.OK);
    }

}
