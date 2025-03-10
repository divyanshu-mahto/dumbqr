package com.dumbqr.dumbqr.service;

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
import java.util.List;

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

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);


    public User register(User user){
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DataIntegrityViolationException("Email already in use");
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String verify(User user){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        if (authentication.isAuthenticated()){
            return jwtService.generateToken(user.getEmail());
        }else{
            return "Fail";
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
