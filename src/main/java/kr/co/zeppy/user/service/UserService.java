package kr.co.zeppy.user.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import kr.co.zeppy.user.dto.UserSignUpDto;
import kr.co.zeppy.user.entity.Role;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void signUp(UserSignUpDto userSignUpDto) throws Exception {

        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        if (userRepository.findByNickname(userSignUpDto.getNickname()).isPresent()) {
            throw new Exception("이미 존재하는 닉네임입니다.");
        }

        User user = User.builder()
                .email(userSignUpDto.getEmail())
                .nickname(userSignUpDto.getNickname())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }
}




