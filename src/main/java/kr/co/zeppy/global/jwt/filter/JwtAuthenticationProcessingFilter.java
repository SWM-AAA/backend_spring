package kr.co.zeppy.global.jwt.filter;

import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.InvalidJwtException;
import kr.co.zeppy.global.error.ApplicationException;
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
    private static final String NO_CHECK_URL2 = "/login-by-username";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().equals(NO_CHECK_URL) || request.getRequestURI().equals(NO_CHECK_URL2)) {
            filterChain.doFilter(request, response);
            return;
        }
        checkTokenAndAuthentication(request, response, filterChain);

        // String refreshToken = jwtService.extractRefreshToken(request)
        //         .filter(jwtService::isTokenValid)
        //         .orElse(null);

        // if (refreshToken != null) {
        //     checkTokenAndAuthentication(request, response, filterChain);
        //     return;
        // }

        // checkAccessTokenAndAuthentication(request, response, filterChain);
    }


    // public void checkAccessTokenAndAuthentication(HttpServletRequest request,
    //         HttpServletResponse response, FilterChain filterChain)
    //         throws ServletException, IOException {
    //     log.info("checkAccessTokenAndAuthentication() 호출");
    //     jwtService.extractAccessToken(request)
    //             .filter(jwtService::isTokenValid)
    //             .ifPresent(accessToken -> jwtService.extractUserTag(accessToken)
    //                     .ifPresent(userTag -> userRepository.findByUserTag(userTag)
    //                             .ifPresent(this::saveAuthentication)));

    //     filterChain.doFilter(request, response);
    // }


    public void checkTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("checkTokenAndAuthentication() 호출");

        Optional<String> accessTokenOpt = jwtService.extractAccessToken(request);
        Optional<String> refreshTokenOpt = jwtService.extractRefreshToken(request);

        log.info(accessTokenOpt.toString());
        log.info(refreshTokenOpt.toString());

        if (accessTokenOpt.isPresent()) {
            String accessToken = accessTokenOpt.get();
            if (!jwtService.isTokenValid(accessToken)) {
                throw new ApplicationException(ApplicationError.INVALID_JWT_TOKEN);
            }

            String userTag = jwtService.extractUserTag(accessToken)
                    .orElseThrow(() -> new ApplicationException(ApplicationError.USER_TAG_NOT_FOUND));

            User user = userRepository.findByUserTag(userTag)
                    .orElseThrow(() -> new ApplicationException(ApplicationError.USER_NOT_FOUND));

            if (jwtService.shouldReissueToken(accessToken)) {
                String newAccessToken = jwtService.createAccessToken(userTag);
                jwtService.setAccessTokenHeader(response, newAccessToken);
            }

            saveAuthentication(user);
        }

        if (refreshTokenOpt.isPresent()) {
            String refreshToken = refreshTokenOpt.get();
            if (!jwtService.isTokenValid(refreshToken)) {
                throw new ApplicationException(ApplicationError.INVALID_JWT_TOKEN);
            }
            if (jwtService.shouldReissueToken(refreshToken)) {
                String newRefreshToken = jwtService.createRefreshToken();
                jwtService.setRefreshTokenHeader(response, newRefreshToken);
            }
        }

        filterChain.doFilter(request, response);
    }


    public void saveAuthentication(User myUser) {
        log.info("saveAuthentication() 호출");
        String password = myUser.getPassword();
        if (password == null) {
            password = PasswordUtil.generateRandomPassword();
        }

        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
                .username(myUser.getUserTag())
                .password(password)
                .roles(myUser.getRole().name())
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetailsUser, null,
                        authoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));

        log.info(authentication.isAuthenticated() + " : 인증 여부");

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
