package com.aaa.zeppy.user.service;

import com.aaa.zeppy.user.entity.Role;
import com.aaa.zeppy.user.entity.User;
import com.aaa.zeppy.user.repository.UserRepository;
import com.aaa.zeppy.user.dto.UserSignUpDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void signUp(UserSignUpDto userSignUpDto) throws Exception {

        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            log.info("이미 존재하는 이메일입니다.");
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(userSignUpDto.getEmail())
                .age(userSignUpDto.getAge())
                .userName(userSignUpDto.getUserName())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }
}