package com.dumbqr.dumbqr.service;

import com.dumbqr.dumbqr.model.User;
import com.dumbqr.dumbqr.model.UserPrinciple;
import com.dumbqr.dumbqr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {


    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if(user == null){
            throw new UsernameNotFoundException("User not found");
        }
        return new UserPrinciple(user);
    }
}
