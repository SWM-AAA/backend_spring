package kr.co.zeppy.oauth2.handler;

import kr.co.zeppy.user.entity.Role;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;
import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.oauth2.entity.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${jwt.access.accesstoken_name}")
    private String accessTokenName;

    @Value("${jwt.refresh.refreshtoken_name}")
    private String refreshTokenName;

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final String CALLBACK_URL = "com.aaa://";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String accessToken = jwtService.createAccessToken(oAuth2User.getUserTag());
        String refreshToken = jwtService.createRefreshToken();
        User findUser = userRepository.findByUserTag(oAuth2User.getUserTag())
                        .orElseThrow(() -> new IllegalArgumentException("ID 에 해당하는 유저가 없습니다."));
        findUser.updateRefreshToken(refreshToken);
        userRepository.saveAndFlush(findUser);

        if (oAuth2User.getRole() == Role.GUEST) {
            String url = jwtService.setAccessTokenAndRefreshTokenURLParam(CALLBACK_URL, accessToken, refreshToken, true);
            log.info("accessToken : 엑세스 토큰 : " + accessToken);
            log.info("refreshToken : 리프레시 토큰 : " + refreshToken);
            response.sendRedirect(url);
        }
        else {
            String url = jwtService.setAccessTokenAndRefreshTokenURLParam(CALLBACK_URL, accessToken, refreshToken, false);
            log.info("accessToken : 엑세스 토큰 : " + accessToken);
            log.info("refreshToken : 리프레시 토큰 : " + refreshToken);
            response.sendRedirect(url);
        }
    }

    private void loginSuccess(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        String accessToken = jwtService.createAccessToken(oAuth2User.getUserTag());
        String refreshToken = jwtService.createRefreshToken();
        log.info("accessToken : 엑세스 토큰 : " + accessToken);
        log.info("refreshToken : 리프레시 토큰 : " + refreshToken);

        jwtService.updateRefreshToken(oAuth2User.getUserTag(), refreshToken);
        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
    }
}
