package com.backend.social.service;

import com.backend.social.dto.request.OtpVerifyRequest;
import com.backend.social.entity.Otp;
import com.backend.social.entity.User;
import com.backend.social.enums.OtpReason;
import com.backend.social.enums.OtpStatus;
import com.backend.social.enums.OtpType;
import com.backend.social.exception.InvalidOtpException;
import com.backend.social.repository.OtpRepository;
import com.backend.social.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpVerificationServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OtpVerificationService otpVerificationService;

    // ---------- TEST HELPERS ----------

    private User validUser() {
        User user = new User();
        user.setId(1L);
        return user;
    }

    private OtpVerifyRequest validRequest(OtpReason reason) {
        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setOtp("123456");
        req.setOtpType(OtpType.EMAIL);
        req.setOtpReason(reason);
        return req;
    }

    private Otp validOtp() {
        Otp otp = new Otp();
        otp.setUserId(1L);
        otp.setOtpType(OtpType.EMAIL);
        otp.setOtpReason(OtpReason.REGISTRATION);
        otp.setStatus(OtpStatus.GENERATED);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otp.setRetryCount(0);
        otp.setOtpHash(BCrypt.hashpw("123456", BCrypt.gensalt()));
        return otp;
    }

    // ---------- TC-01: OTP NOT FOUND ----------

    @Test
    void verifyOtp_shouldThrowException_whenOtpNotFound() {

        when(otpRepository.findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
                any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidOtpException.class,
                () -> otpVerificationService.verifyOtp(validUser(), validRequest(OtpReason.REGISTRATION)));
    }

    // ---------- TC-02: OTP ALREADY USED / BLOCKED ----------

    @Test
    void verifyOtp_shouldThrowException_whenOtpAlreadyUsed() {

        Otp otp = validOtp();
        otp.setStatus(OtpStatus.VERIFIED);

        when(otpRepository.findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
                any(), any(), any()))
                .thenReturn(Optional.of(otp));

        assertThrows(InvalidOtpException.class,
                () -> otpVerificationService.verifyOtp(validUser(), validRequest(OtpReason.REGISTRATION)));
    }

    // ---------- TC-03: OTP EXPIRED ----------

    @Test
    void verifyOtp_shouldExpireOtp_whenExpired() {

        Otp otp = validOtp();
        otp.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(otpRepository.findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
                any(), any(), any()))
                .thenReturn(Optional.of(otp));

        InvalidOtpException ex = assertThrows(
                InvalidOtpException.class,
                () -> otpVerificationService.verifyOtp(validUser(), validRequest(OtpReason.REGISTRATION))
        );

        assertEquals(OtpStatus.EXPIRED, otp.getStatus());
        verify(otpRepository).save(otp);
        assertEquals("OTP expired", ex.getMessage());
    }

    // ---------- TC-04: INVALID OTP (RETRY < MAX) ----------

    @Test
    void verifyOtp_shouldIncrementRetryCount_whenOtpInvalid() {

        Otp otp = validOtp();

        when(otpRepository.findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
                any(), any(), any()))
                .thenReturn(Optional.of(otp));

        OtpVerifyRequest req = validRequest(OtpReason.REGISTRATION);
        req.setOtp("999999"); // wrong otp

        assertThrows(InvalidOtpException.class,
                () -> otpVerificationService.verifyOtp(validUser(), req));

        assertEquals(1, otp.getRetryCount());
        verify(otpRepository).save(otp);
    }

    // ---------- TC-05: INVALID OTP → BLOCKED ----------

    @Test
    void verifyOtp_shouldBlockOtp_whenMaxRetriesReached() {

        Otp otp = validOtp();
        otp.setRetryCount(2);

        when(otpRepository.findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
                any(), any(), any()))
                .thenReturn(Optional.of(otp));

        OtpVerifyRequest req = validRequest(OtpReason.REGISTRATION);
        req.setOtp("999999");

        assertThrows(InvalidOtpException.class,
                () -> otpVerificationService.verifyOtp(validUser(), req));

        assertEquals(OtpStatus.BLOCKED, otp.getStatus());
        verify(otpRepository).save(otp);
    }

    // ---------- TC-06: SUCCESSFUL OTP VERIFICATION ----------

    @Test
    void verifyOtp_shouldVerifyOtp_whenValid() {

        Otp otp = validOtp();

        when(otpRepository.findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
                any(), any(), any()))
                .thenReturn(Optional.of(otp));

        otpVerificationService.verifyOtp(validUser(), validRequest(OtpReason.REGISTRATION));

        assertEquals(OtpStatus.VERIFIED, otp.getStatus());
        verify(otpRepository).save(otp);
        verify(userRepository, never()).save(any());
    }

    // ---------- TC-07: PASSWORD RESET OTP ----------

    @Test
    void verifyOtp_shouldGrantPasswordReset_whenOtpReasonPasswordReset() {

        Otp otp = validOtp();
        otp.setOtpReason(OtpReason.PASSWORD_RESET);

        User user = validUser();

        when(otpRepository.findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
                any(), any(), any()))
                .thenReturn(Optional.of(otp));

        otpVerificationService.verifyOtp(user, validRequest(OtpReason.PASSWORD_RESET));

        assertTrue(user.isPasswordResetAllowed());
        assertNotNull(user.getPasswordResetExpiresAt());
        verify(userRepository).save(user);
    }
}

