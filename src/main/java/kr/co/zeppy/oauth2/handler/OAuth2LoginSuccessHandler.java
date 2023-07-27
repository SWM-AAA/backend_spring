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
import org.springframework.web.util.UriComponentsBuilder;

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
        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

            if(oAuth2User.getRole() == Role.GUEST) {
                String accessToken = jwtService.createAccessToken(oAuth2User.getEmail(), oAuth2User.getNickname());
                String refreshToken = jwtService.createRefreshToken();

                jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
                User findUser = userRepository.findByEmail(oAuth2User.getEmail())
                               .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 유저가 없습니다."));

                findUser.updateRefreshToken(refreshToken);
                findUser.authorizeUser();
                userRepository.saveAndFlush(findUser);

                String url = UriComponentsBuilder.fromUriString(CALLBACK_URL)
                        .queryParam(accessTokenName, accessToken)
                        .queryParam(refreshTokenName, refreshToken)
                        .build().toUriString();

                response.sendRedirect(url);
                // response.sendRedirect(CALLBACK_URL);
            } else if (oAuth2User.getRole() == Role.USER) {
                loginSuccess(response, oAuth2User); // 로그인에 성공한 경우 access, refresh 토큰 생성
                response.sendRedirect(CALLBACK_URL);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    // TODO : 소셜 로그인 시에도 무조건 토큰 생성하지 말고 JWT 인증 필터처럼 RefreshToken 유/무에 따라 다르게 처리해보기
    private void loginSuccess(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail(), oAuth2User.getNickname());
        String refreshToken = jwtService.createRefreshToken();
        response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
        response.addHeader(jwtService.getRefreshHeader(), "Bearer " + refreshToken);

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        jwtService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);
    }
}
