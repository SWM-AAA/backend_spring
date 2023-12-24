package kr.co.zeppy.global.configuration;

import kr.co.zeppy.global.jwt.service.JwtService;
import kr.co.zeppy.global.filter.ExceptionHandlerFilter;
import kr.co.zeppy.global.jwt.filter.JwtAuthenticationProcessingFilter;
import kr.co.zeppy.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import kr.co.zeppy.user.repository.UserRepository;
import kr.co.zeppy.oauth2.handler.OAuth2LoginFailureHandler;
import kr.co.zeppy.oauth2.handler.OAuth2LoginSuccessHandler;
import kr.co.zeppy.oauth2.service.CustomOAuth2UserService;
import kr.co.zeppy.user.service.LoginService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginService loginService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    private static final String[] AUTH_WHITELIST = {
            "/login",
            "/api/healthcheck",
            "/api/test/**",
            "/api/v1/users/all-user-location-and-battery",
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .formLogin(formLogin -> formLogin.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()).disable())
            .sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(request -> request
                .requestMatchers(AUTH_WHITELIST).permitAll()
            .anyRequest().authenticated())
            .oauth2Login(oauth2Login -> oauth2Login
                .authorizationEndpoint()
                .authorizationRequestRepository(new HttpCookieOAuth2AuthorizationRequestRepository())
                .and()
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler)
                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(customOAuth2UserService)));

        return http
                .addFilterBefore(jwtAuthenticationProcessingFilter(), LogoutFilter.class)
                .addFilterBefore(exceptionHandlerFilter(), JwtAuthenticationProcessingFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(loginService);
        return new ProviderManager(provider);
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(jwtService, userRepository);
    }

    @Bean
    public ExceptionHandlerFilter exceptionHandlerFilter() {
        return new ExceptionHandlerFilter(objectMapper);
    }
}
