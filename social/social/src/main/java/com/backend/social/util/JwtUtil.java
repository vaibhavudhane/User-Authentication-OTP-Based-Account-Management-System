package com.backend.social.util;

import com.backend.social.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET =
            "SUPER_SECRET_KEY_CHANGE_THIS_IN_PROD_32_CHARS_MIN";
    private static final long EXPIRY = 60 * 60 * 1000; // 1 hour


    public String generateToken(Long userId, String username, Role role) {

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRY))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();
    }


    /* ================= TOKEN PARSING ================= */

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //Extract username/email
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extract userId
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    //Extract role
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }


}


