package com.backend.social.util;

import com.backend.social.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwtUtil;

    public JwtUtilTest(JwtUtil jwtUtil)
    {
        this.jwtUtil=jwtUtil;
    }

    // ---------- TC-01: TOKEN GENERATION ----------

    @Test
    void generateToken_shouldReturnNonNullToken() {

        String token = jwtUtil.generateToken(
                1L,
                "vaibhav",
                Role.USER
        );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    // ---------- TC-02: EXTRACT USERNAME ----------

    @Test
    void extractUsername_shouldReturnCorrectUsername() {

        String token = jwtUtil.generateToken(
                1L,
                "vaibhav",
                Role.USER
        );

        String username = jwtUtil.extractUsername(token);

        assertEquals("vaibhav", username);
    }

    // ---------- TC-03: EXTRACT USER ID ----------

    @Test
    void extractUserId_shouldReturnCorrectUserId() {

        String token = jwtUtil.generateToken(
                99L,
                "admin",
                Role.ADMIN
        );

        Long userId = jwtUtil.extractUserId(token);

        assertEquals(99L, userId);
    }

    // ---------- TC-04: EXTRACT ROLE ----------

    @Test
    void extractRole_shouldReturnCorrectRole() {

        String token = jwtUtil.generateToken(
                5L,
                "admin",
                Role.ADMIN
        );

        String role = jwtUtil.extractRole(token);

        assertEquals("ADMIN", role);
    }
}

