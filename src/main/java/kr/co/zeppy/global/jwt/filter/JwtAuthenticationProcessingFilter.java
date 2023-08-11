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
            // checkRefreshTokenAndReIssueAccessToken(request, response, refreshToken, filterChain);
            checkAccessTokenAndAuthentication(request, response, filterChain);
            return;
        }
        
        checkAccessTokenAndAuthentication(request, response, filterChain);
    }


    public void checkRefreshTokenAndReIssueAccessToken(HttpServletRequest request, HttpServletResponse response, String refreshToken,
                                                         FilterChain filterChain) throws ServletException, IOException {
        Optional<User> userOptional = userRepository.findByRefreshToken(refreshToken);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String reIssuedRefreshToken = reIssueRefreshToken(user);
            jwtService.sendAccessAndRefreshToken(response, jwtService.createAccessToken(user.getUserTag()),
                    reIssuedRefreshToken);
        }
    }


    private String reIssueRefreshToken(User user) {
        String reIssuedRefreshToken = jwtService.createRefreshToken();
        user.updateRefreshToken(reIssuedRefreshToken);
        userRepository.saveAndFlush(user);
        return reIssuedRefreshToken;
    }

    public void checkAccessTokenAndAuthentication(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("checkAccessTokenAndAuthentication() 호출");
        jwtService.extractAccessToken(request)
                .filter(jwtService::isTokenValid)
                .ifPresent(accessToken -> jwtService.extractUserTag(accessToken)
                        .ifPresent(userTag -> userRepository.findByUserTag(userTag)
                                .ifPresent(this::saveAuthentication)));

        filterChain.doFilter(request, response);
    }


    public void saveAuthentication(User myUser) {
        log.info("saveAuthentication() 호출");
        String password = PasswordUtil.generateRandomPassword();

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
