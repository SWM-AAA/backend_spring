package kr.co.zeppy.global.jwt.filter;

import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.global.jwt.util.PasswordUtil;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;


@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private static final String NO_CHECK_URL = "/login";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().equals(NO_CHECK_URL)) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        if (refreshToken != null) {
            checkRefreshTokenAndReIssueAccessToken(request, response, refreshToken, filterChain);
        }
        
        if (refreshToken == null) {
            checkAccessTokenAndAuthentication(request, response, filterChain);
        }
    }


    public void checkRefreshTokenAndReIssueAccessToken(HttpServletRequest request, HttpServletResponse response, String refreshToken,
                                                         FilterChain filterChain) throws ServletException, IOException {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String reIssuedRefreshToken = reIssueRefreshToken(user);
                    jwtService.sendAccessAndRefreshToken(response, jwtService.createAccessToken(user.getEmail(), user.getNickname()),
                            reIssuedRefreshToken);
                });
        // filterChain.doFilter(request, response);
    }


    private String reIssueRefreshToken(User user) {
        String reIssuedRefreshToken = jwtService.createRefreshToken();
        user.updateRefreshToken(reIssuedRefreshToken);
        userRepository.saveAndFlush(user);
        return reIssuedRefreshToken;
    }


    // public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
    //                                               FilterChain filterChain) throws ServletException, IOException {
    //     log.info("checkAccessTokenAndAuthentication() 호출");
    //     jwtService.extractAccessToken(request)
    //             .filter(jwtService::isTokenValid)
    //             .ifPresent(accessToken -> jwtService.extractEmail(accessToken)
    //                     .ifPresent(email -> userRepository.findByEmail(email)
    //                             .ifPresent(this::saveAuthentication)));
    //     log.info("checkAccessTokenAndAuthentication() 종료");

    //     filterChain.doFilter(request, response);
    // }

    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              FilterChain filterChain) throws ServletException, IOException {
        log.info("checkAccessTokenAndAuthentication() 호출");

        Optional<String> accessTokenOptional = jwtService.extractAccessToken(request);

        log.info("accessTokenOptional : " + accessTokenOptional);
        if (accessTokenOptional.isPresent()) {
            String accessToken = accessTokenOptional.get();
            boolean isTokenValid = jwtService.isTokenValid(accessToken);

            if (isTokenValid) {
                log.info("Access Token이 유효합니다. Access Token: {}");
                jwtService.extractEmail(accessToken).ifPresent(email -> {
                    Optional<User> userOptional = userRepository.findByEmail(email);
                    log.info("이메일로 사용자 정보를 조회합니다. 이메일: {}", email);

                    if (userOptional.isPresent()) {
                        // 5. 사용자 정보가 있으면 인증 처리
                        log.info("사용자 정보를 조회했습니다. 이메일: {}", email);
                        User user = userOptional.get();
                        saveAuthentication(user);
                    } else {
                        log.warn("사용자 정보가 없습니다. 이메일: {}", email);
                    }
                });
            } else {
                log.warn("유효하지 않은 Access Token입니다. Access Token: {}", accessToken);
            }
        } else {
            log.warn("Access Token이 요청에 없습니다.");
        }

        log.info("checkAccessTokenAndAuthentication() 종료");

        filterChain.doFilter(request, response);
    }


    /**
     * [인증 허가 메소드]
     * 파라미터의 유저 : 우리가 만든 회원 객체 / 빌더의 유저 : UserDetails의 User 객체
     *
     * new UsernamePasswordAuthenticationToken()로 인증 객체인 Authentication 객체 생성
     * UsernamePasswordAuthenticationToken의 파라미터
     * 1. 위에서 만든 UserDetailsUser 객체 (유저 정보)
     * 2. credential(보통 비밀번호로, 인증 시에는 보통 null로 제거)
     * 3. Collection < ? extends GrantedAuthority>로,
     * UserDetails의 User 객체 안에 Set<GrantedAuthority> authorities이 있어서 getter로 호출한 후에,
     * new NullAuthoritiesMapper()로 GrantedAuthoritiesMapper 객체를 생성하고 mapAuthorities()에 담기
     *
     * SecurityContextHolder.getContext()로 SecurityContext를 꺼낸 후,
     * setAuthentication()을 이용하여 위에서 만든 Authentication 객체에 대한 인증 허가 처리
     */
    public void saveAuthentication(User myUser) {
        log.info("saveAuthentication() 호출");
        String password = PasswordUtil.generateRandomPassword();

        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
                .username(myUser.getEmail())
                .password(password)
                .roles(myUser.getRole().name())
                .build();

        log.info("userDetailsUser : 호출");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetailsUser, null,
                authoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));
        
        log.info(authentication.isAuthenticated() + " : 인증 여부");

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
