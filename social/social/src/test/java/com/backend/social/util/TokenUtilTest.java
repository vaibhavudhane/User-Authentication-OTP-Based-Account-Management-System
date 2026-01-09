package com.backend.social.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenUtilTest {

    // ---------- TC-01: TOKEN GENERATION ----------

    @Test
    void generateToken_shouldReturnNonNullToken() {

        String token = TokenUtil.generateToken();

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    // ---------- TC-02: TOKEN FORMAT (URL SAFE) ----------

    @Test
    void generateToken_shouldBeUrlSafeBase64() {

        String token = TokenUtil.generateToken();

        // URL-safe Base64 should not contain + / =
        assertFalse(token.contains("+"));
        assertFalse(token.contains("/"));
        assertFalse(token.contains("="));
    }

    // ---------- TC-03: HASH IS DETERMINISTIC ----------

    @Test
    void hashToken_shouldReturnSameHashForSameInput() {

        String token = "test-token";

        String hash1 = TokenUtil.hashToken(token);
        String hash2 = TokenUtil.hashToken(token);

        assertEquals(hash1, hash2);
    }

    // ---------- TC-04: DIFFERENT TOKENS → DIFFERENT HASH ----------

    @Test
    void hashToken_shouldReturnDifferentHashForDifferentTokens() {

        String hash1 = TokenUtil.hashToken("token-1");
        String hash2 = TokenUtil.hashToken("token-2");

        assertNotEquals(hash1, hash2);
    }

    // ---------- TC-05: HASH NOT NULL ----------

    @Test
    void hashToken_shouldReturnNonNullHash() {

        String hash = TokenUtil.hashToken("any-token");

        assertNotNull(hash);
        assertFalse(hash.isBlank());
    }
}

