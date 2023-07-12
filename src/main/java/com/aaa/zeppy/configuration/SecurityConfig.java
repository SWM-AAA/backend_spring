package com.aaa.zeppy.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.aaa.zeppy.user.entity.SocialType;
import com.aaa.zeppy.common.jwt.service.JwtService;
import com.aaa.zeppy.common.jwt.filter.JwtAuthenticationProcessingFilter;
import com.aaa.zeppy.user.repository.UserRepository;
import com.aaa.zeppy.OAuth2.handler.OAuth2LoginFailureHandler;
import com.aaa.zeppy.OAuth2.handler.OAuth2LoginSuccessHandler;
import com.aaa.zeppy.OAuth2.service.CustomOAuth2UserService;
import com.aaa.zeppy.user.service.UserDetailsServiceImpl;

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

/**
 * JwtAuthenticationProcessingFilter는 AccessToken, RefreshToken 재발급
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .formLogin(formLogin -> formLogin.disable()) // FormLogin 사용 X
            .httpBasic(httpBasic -> httpBasic.disable()) // httpBasic 사용 X
            .csrf(csrf -> csrf.disable()) // csrf 보안 사용 X
            .headers(headers -> headers.frameOptions().disable())
            // 세션 사용하지 않으므로 STATELESS로 설정
            .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            //== URL별 권한 관리 옵션 ==//
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests.anyRequest().authenticated())

            // 아이콘, css, js 관련
            // 기본 페이지, css, image, js 하위 폴더에 있는 자료들은 모두 접근 가능, h2-console에 접근 가능
            // .antMatchers("/","/css/**","/images/**","/js/**","/favicon.ico","/h2-console/**").permitAll()
            // .antMatchers("/sign-up").permitAll() // 회원가입 접근 가능
            // 위의 경로 이외에는 모두 인증된 사용자만 접근 가능
            //== 소셜 로그인 설정 ==//
            .oauth2Login(oauth2Login -> oauth2Login
                .successHandler(oAuth2LoginSuccessHandler) // 동의하고 계속하기를 눌렀을 때 Handler 설정
                .failureHandler(oAuth2LoginFailureHandler) // 소셜 로그인 실패 시 핸들러 설정
                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(customOAuth2UserService))); // customUserService 설정

        // 원래 스프링 시큐리티 필터 순서가 LogoutFilter 이후에 로그인 필터 동작
        // 따라서, LogoutFilter 이후에 우리가 만든 필터 동작하도록 설정
        // 순서 : LogoutFilter -> JwtAuthenticationProcessingFilter -> CustomJsonUsernamePasswordAuthenticationFilter
        http.addFilter(jwtAuthenticationProcessingFilter());

        return http.build();
    }

    /**
     * AuthenticationManager 설정 후 등록
     * PasswordEncoder를 사용하는 AuthenticationProvider 지정 (PasswordEncoder는 위에서 등록한 PasswordEncoder 사용)
     * FormLogin(기존 스프링 시큐리티 로그인)과 동일하게 DaoAuthenticationProvider 사용
     * UserDetailsService는 커스텀 LoginService로 등록
     * 또한, FormLogin과 동일하게 AuthenticationManager로는 구현체인 ProviderManager 사용(return ProviderManager)
     *
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsServiceImpl);
        return new ProviderManager(provider);
    }

    /**
     * 커스텀 필터를 사용하기 위해 만든 커스텀 필터를 Bean으로 등록
     */
    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        JwtAuthenticationProcessingFilter jwtAuthenticationFilter = new JwtAuthenticationProcessingFilter(jwtService, userRepository);
        return jwtAuthenticationFilter;
    }
}