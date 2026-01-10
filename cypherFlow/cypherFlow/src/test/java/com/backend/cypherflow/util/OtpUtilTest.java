package com.backend.cypherflow.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpUtilTest {

    // ---------- TC-01: NOT NULL & NOT EMPTY ----------

    @Test
    void generateOtp_shouldReturnNonNullOtp() {

        String otp = OtpUtil.generateOtp();

        assertNotNull(otp);
        assertFalse(otp.isBlank());
    }

    // ---------- TC-02: OTP LENGTH ----------

    @Test
    void generateOtp_shouldReturnSixDigitOtp() {

        String otp = OtpUtil.generateOtp();

        assertEquals(6, otp.length());
    }

    // ---------- TC-03: ONLY DIGITS ----------

    @Test
    void generateOtp_shouldContainOnlyDigits() {

        String otp = OtpUtil.generateOtp();

        assertTrue(otp.matches("\\d{6}"));
    }

    // ---------- TC-04: RANGE VALIDATION ----------

    @Test
    void generateOtp_shouldBeWithinValidRange() {

        String otp = OtpUtil.generateOtp();
        int value = Integer.parseInt(otp);

        assertTrue(value >= 100000 && value <= 999999);
    }

    // ---------- TC-05: MULTIPLE CALLS ----------

    @Test
    void generateOtp_shouldGenerateDifferentValues() {

        String otp1 = OtpUtil.generateOtp();
        String otp2 = OtpUtil.generateOtp();

        // Not guaranteed to be different every time,
        // but extremely unlikely to collide in two calls
        assertNotEquals(otp1, otp2);
    }
}


