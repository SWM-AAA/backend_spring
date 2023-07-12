package com.aaa.zeppy.user.service;

import org.springframework.stereotype.Service;
import com.aaa.zeppy.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.aaa.zeppy.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> optionaluser = userRepository.findByEmail(email);
        User user = optionaluser.get();
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserName())
                // .email(user.getEmail())
                .roles(user.getRole().toString())
                .build();
    }
}
