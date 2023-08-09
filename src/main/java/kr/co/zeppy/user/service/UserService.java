package kr.co.zeppy.user.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.ApplicationException;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.UserRegisterRequest;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public void register(String accessToken, UserRegisterRequest userRegisterRequest) {
        String userTag = jwtService.extractUserTag(accessToken)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_LOGINID_NOT_FOUND));

        User user = userRepository.findByUserTag(userTag)
                .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));
        user.updateNickname(userRegisterRequest.getNickname());
        user.updateImageUrl(userRegisterRequest.getImageUrl());
    }
}
