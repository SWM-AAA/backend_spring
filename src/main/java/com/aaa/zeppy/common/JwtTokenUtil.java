package com.aaa.zeppy.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// import com.aaa.zeppy.member.entity.Member;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

    @Value("${jwt.secret}")
    private String secret;

    // 토큰에서 사용자 이름을 추출하는 함수
    public String getMemberNameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // 토큰에서 이메일을 추출하는 함수
    public String getEmailFromToken(String token) {
        // (String) --> 앞의 함수의 반환값을 String으로 캐스팅함.
        return (String) getClaimFromToken(token, claims -> claims.get("email", String.class));
    }

    // 토큰에서 소셜 로그인 정보를 추출하는 함수
    public String getProviderFromToken(String token) {
        return (String) getClaimFromToken(token, claims -> claims.get("provider", String.class));
    }

    // 토큰에서 만료 일자를 추출하는 함수
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // 토큰에서 주어진 클레임 정보를 추출하는 함수
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // 토큰의 모든 클레임 정보를 추출하는 함수
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    // 토큰이 만료되었는지 확인하는 함수
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberName", member.getMembername());
        claims.put("email", member.getEmail());
        claims.put("provider", member.getProvider());
        return doGenerateToken(claims);
    }

    // 토큰을 실제로 생성하는 함수
    private String doGenerateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // 토큰의 유효성을 검사하는 함수
    public Boolean validateToken(String token, Member member) {
        final String memberName = getMemberNameFromToken(token);
        return (memberName.equals(member.getEmail()) && !isTokenExpired(token));
    }
}
