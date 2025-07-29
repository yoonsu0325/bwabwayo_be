package com.bwabwayo.app.domain.user.utils;

import com.bwabwayo.app.domain.user.dto.response.OAuth2UserResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Component
public class JWTUtils {
    private SecretKey secretKey;

    public JWTUtils(@Value("${jwt.secret}") String secret){
        byte[] keyBytes = Decoders.BASE64.decode(secret); // BASE64 디코딩
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(OAuth2UserResponse user, int validTime, String role) {
        return Jwts.builder()
                .setHeader(Map.of("typ","JWT"))
                .setSubject(user.getId())
                .claim("role", role)
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(validTime).toInstant()))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // 토큰이 유효하지 않은 경우
        }
    }

    public boolean isExpired(String token) {
        try {
            validateToken(token);
        } catch (Exception e) {
            return (e instanceof ExpiredJwtException);
        }
        return false;
    }

    public long tokenRemainTime(Integer expTime) {
        Date expDate = new Date((long) expTime * (1000));
        long remainMs = expDate.getTime() - System.currentTimeMillis();
        return remainMs / (1000 * 60);
    }

    public String getClaims(String jwt) {
        try{
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody()
                    .getSubject();

        }
        catch (JwtException e){
            return null;
        }
    }

    public static ResponseCookie createHttpOnlyCookie(String refreshToken) { // 수정
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(1))
                .sameSite("Strict") //일반적으로는 "Lax"사용
                .build();
    }
}
