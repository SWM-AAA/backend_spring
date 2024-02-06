package kr.co.zeppy.login.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${jwt.access.expiration}")
    private String accessTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        log.info("아이디-패스워드 Login 성공!");
        String username = extractUsername(authentication);

        String userTag = userRepository.findUserTagByUsername(username).toString();
        Optional<String> userTagOpt = userRepository.findUserTagByUsername(username);
        if (userTagOpt.isPresent()) {
            userTag = userTagOpt.get();
        }
        String accessToken = jwtService.createAccessToken(userTag);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    user.updateRefreshToken(refreshToken);
                    userRepository.saveAndFlush(user);
                });

        log.info("username : 아이디 : " + username);
        log.info("accessToken : 엑세스 토큰 : " + accessToken);
        log.info("refreshToken : 리프레시 토큰 : " + refreshToken);
    }

    private String extractUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}