package kr.co.zeppy.user.service;

import kr.co.zeppy.global.jwt.util.PasswordUtil;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 Login ID 가 존재하지 않습니다."));
        
        String password = PasswordUtil.generateRandomPassword();
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLoginId())
                .password(password)
                .roles(user.getRole().toString())
                .build();
    }
}
