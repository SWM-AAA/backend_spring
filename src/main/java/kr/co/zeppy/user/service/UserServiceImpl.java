package kr.co.zeppy.user.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.dto.UserRegisterRequest;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void register(UserRegisterRequest userRegisterRequest) {
        
    }
}
