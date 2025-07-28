package com.bwabwayo.app.domain.user.utils;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;

@Component
public class JWTUtils {
    private SecretKey secretKey;

    public JWTUtils(@Value("${jwt.secret}") String secret){
        byte[] keyBytes = Decoders.BASE64.decode(secret); // BASE64 디코딩
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }
}
