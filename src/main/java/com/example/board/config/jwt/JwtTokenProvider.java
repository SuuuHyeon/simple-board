package com.example.board.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long tokenValidityInMilliseconds;

    /// yml에서 설정 값 주입
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-ms}") long tokenValidityInMilliseconds
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);    // Base64로 인코딩된 비밀 키를 디코딩 후 'Key' 객체로 변환
        this.key = Keys.hmacShaKeyFor(keyBytes);                // HMAC-SHA 알고리즘으로 Key 생성
        this.tokenValidityInMilliseconds = tokenValidityInMilliseconds;
    }

    /// 토근 생성
    public String createToken(String email, Long memberId) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        String createdToken = Jwts.builder()
                .subject(email)
                .claim("id", memberId)
                .issuedAt(new Date(now))
                .expiration(validity)
                .signWith(key)
                .compact();
        log.info("createdToken: {}", createdToken);
        return createdToken;
    }


    /// 토근 검증 및 정보 추출
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(this.key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String email = claims.getSubject();     // 생성 때 넣었던 email
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");   // (임시)

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                email, "", Collections.singletonList(authority)
        );

        log.info("토큰 검증 / 추출: {}", usernamePasswordAuthenticationToken);

        // email을 principal로 사용
        ///  TODO: 다시보기
        return usernamePasswordAuthenticationToken;
    }


    /// 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser().verifyWith(this.key).build().parseSignedClaims(token);
            log.info("토큰 유효성 검사: {}", claimsJws.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
