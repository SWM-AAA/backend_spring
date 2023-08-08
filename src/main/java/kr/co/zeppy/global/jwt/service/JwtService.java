package kr.co.zeppy.global.jwt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import kr.co.zeppy.global.error.ApplicationError;
import kr.co.zeppy.global.error.InvalidJwtException;
import kr.co.zeppy.user.entity.User;
import kr.co.zeppy.user.repository.UserRepository;
import kr.co.zeppy.global.error.NotFoundException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    @Value("${jwt.access.accesstoken_name}")
    private String accessTokenName;

    @Value("${jwt.refresh.refreshtoken_name}")
    private String refreshTokenName;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String LOGIN_ID_CLAIM = "loginId";
    private static final String BEARER = "Bearer ";
    private static final String IS_FIRST = "is_first";
    private static final String APPLICATION_JSON = "application/json";
    private static final String UTF_8 = "UTF-8";

    private final UserRepository userRepository;


    public String createAccessToken(String loginId) {
        Date now = new Date();
        return JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withClaim(LOGIN_ID_CLAIM, loginId)
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }


    public String createRefreshToken() {
        Date now = new Date();
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }


    public String setAccessTokenAndRefreshTokenURLParam(String url, String accessToken, String refreshToken, boolean isfirst) {
        return UriComponentsBuilder.fromUriString(url)
                .queryParam(accessTokenName, accessToken)
                .queryParam(refreshTokenName, refreshToken)
                .queryParam(IS_FIRST, isfirst)
                .build().toUriString();
    }


    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
        log.info("Access Token, Refresh Token 헤더 설정 완료");

        setAccessTokenBody(response, accessToken);
        setRefreshTokenBody(response, refreshToken);
        log.info("Access Token, Refresh Token Body 설정 완료");

    }


    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        String refreshTokenHeaderValue = request.getHeader(refreshHeader);
        Boolean isStartBearer;
        if (refreshTokenHeaderValue == null) {
            isStartBearer = false;
        } else {
            isStartBearer = refreshTokenHeaderValue.startsWith(BEARER);
        }
        Optional<String> refreshToken;

        if (refreshTokenHeaderValue != null && isStartBearer) {
            refreshToken = Optional.of(refreshTokenHeaderValue.replace(BEARER, ""));
        } else {
            refreshToken = Optional.empty();
        }
        return refreshToken;
    }


    public Optional<String> extractAccessToken(HttpServletRequest request) {
        String accessTokenHeaderValue = request.getHeader(accessHeader);
        Boolean isStartBearer;
        if (accessTokenHeaderValue == null) {
            isStartBearer = false;
        } else {
            isStartBearer = accessTokenHeaderValue.startsWith(BEARER);
        }
        Optional<String> accessToken;

        if (accessTokenHeaderValue != null && isStartBearer) {
            accessToken = Optional.of(accessTokenHeaderValue.replace(BEARER, ""));
        } else {
            accessToken = Optional.empty();
        }
        return accessToken;
    }


    public Optional<String> extractLoginId(String accessToken) {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken)
                    .getClaim(LOGIN_ID_CLAIM).asString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

    // access token을 response body에 담아서 보냄
    public void setAccessTokenBody(HttpServletResponse response, String accessToken) {
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);
    
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(accessTokenName, accessToken);

        try {
            String jsonToken = new ObjectMapper().writeValueAsString(tokenMap);
            response.getWriter().write(jsonToken);
            log.info("body에 담아서 보낸 jsontoken : {}", jsonToken);
        } catch (Exception e) {
            log.error("Access Token을 Response Body에 담아서 보내는데 실패했습니다. {}", e.getMessage());
        }
    }


    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }


    public void setRefreshTokenBody(HttpServletResponse response, String refreshToken) {
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(refreshTokenName, refreshToken);

        try {
            String jsonToken = new ObjectMapper().writeValueAsString(tokenMap);
            response.getWriter().write(jsonToken);
            log.info("body에 담아서 보낸 jsontoken : {}", jsonToken);
        } catch (Exception e) {
            log.error("refresh Token을 Response Body에 담아서 보내는데 실패했습니다. {}", e.getMessage());
        }
    }


    public void updateRefreshToken(String loginId, String refreshToken) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new NotFoundException(ApplicationError.USER_LOGINID_NOT_FOUND));
        user.updateRefreshToken(refreshToken);
    }

    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다. {}", e.getMessage());
            throw new InvalidJwtException(ApplicationError.INVALID_JWT_TOKEN);
        }
    }
}
