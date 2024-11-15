package com.dumbqr.dumbqr.service;

import com.dumbqr.dumbqr.model.QrCode;
import com.dumbqr.dumbqr.model.User;
import com.dumbqr.dumbqr.repository.QrCodeRepository;
import com.dumbqr.dumbqr.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtService jwtService;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);


    public User register(User user){
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

    public void updateQrCodeList(User user){
        userRepository.save(user);
    }


    public ResponseEntity<List<QrCode>> getAllQr(User user) {
        List<ObjectId> qrids = user.getQrcodes();
        List<QrCode> qrs = new ArrayList<>();

        for (ObjectId id : qrids) {
            qrCodeRepository.findById(id).ifPresent(qrs::add);
        }

        if (qrs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(qrs, HttpStatus.OK);
    }


    public ResponseEntity<?> updateUser(User user){
        userRepository.save(user);
        return new ResponseEntity<>("User details updated",HttpStatus.OK);
    }

    public ResponseEntity<?> deleteUser(User user){

        //delete the qr codes of user
        List<ObjectId> qrcodes = user.getQrcodes();
        for (ObjectId qrcode : qrcodes){
            qrCodeRepository.deleteById(qrcode);
        }

        userRepository.delete(user);
        return new ResponseEntity<>("User deleted",HttpStatus.OK);
    }

}
