package com.backend.social.service;

import com.backend.social.entity.Otp;
import com.backend.social.entity.User;
import com.backend.social.enums.*;
import com.backend.social.exception.UserNotFoundException;
import com.backend.social.repository.OtpRepository;
import com.backend.social.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OtpService otpService;

    // ---------- REFLECTION HELPER ----------

    private OtpDispatchResult invokeGenerateOtp(Long userId, OtpType type, OtpReason reason)
            throws Exception {

        Method method = OtpService.class
                .getDeclaredMethod("generateAndSendOtp", Long.class, OtpType.class, OtpReason.class);

        method.setAccessible(true);
        try {
            return (OtpDispatchResult) method.invoke(otpService, userId, type, reason);
        } catch (InvocationTargetException ex) {
            throw (Exception) ex.getCause(); // unwrap real exception
        }

    }

    // ---------- TEST HELPERS ----------

    private User validUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setMobile("9999999999");
        return user;
    }

    // ---------- TC-01: USER NOT FOUND ----------

    @Test
    void generateOtp_shouldThrowException_whenUserNotFound() throws Exception {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> invokeGenerateOtp(1L, OtpType.EMAIL, OtpReason.REGISTRATION));
    }

    // ---------- TC-02: COOLDOWN ACTIVE ----------

    @Test
    void generateOtp_shouldReturnCooldown_whenCooldownActive() throws Exception {

        User user = validUser();

        Otp lastOtp = Otp.builder()
                .lastSentAt(LocalDateTime.now().minusSeconds(10))
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(otpRepository.findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
                any(), any(), any()))
                .thenReturn(Optional.of(lastOtp));

        OtpDispatchResult result =
                invokeGenerateOtp(1L, OtpType.EMAIL, OtpReason.REGISTRATION);

        assertEquals(OtpDispatchResult.COOLDOWN, result);

        verify(otpRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    // ---------- TC-03: RATE LIMIT EXCEEDED ----------

    @Test
    void generateOtp_shouldReturnRateLimited_whenLimitExceeded() throws Exception {

        User user = validUser();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(otpRepository.findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
                any(), any(), any()))
                .thenReturn(Optional.empty());

        when(otpRepository.countByUserIdAndOtpTypeAndCreatedAtAfter(
                any(), any(), any()))
                .thenReturn(5L);

        OtpDispatchResult result =
                invokeGenerateOtp(1L, OtpType.EMAIL, OtpReason.REGISTRATION);

        assertEquals(OtpDispatchResult.RATE_LIMITED, result);

        verify(otpRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    // ---------- TC-04: OTP SENT VIA EMAIL ----------

    @Test
    void generateOtp_shouldSendEmailOtp_whenAllowed() throws Exception {

        User user = validUser();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(otpRepository.findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
                any(), any(), any()))
                .thenReturn(Optional.empty());

        when(otpRepository.countByUserIdAndOtpTypeAndCreatedAtAfter(
                any(), any(), any()))
                .thenReturn(0L);

        OtpDispatchResult result =
                invokeGenerateOtp(1L, OtpType.EMAIL, OtpReason.REGISTRATION);

        assertEquals(OtpDispatchResult.SENT, result);

        verify(otpRepository).save(any(Otp.class));
        verify(emailService).sendEmail(
                eq("test@gmail.com"),
                eq("OTP Verification"),
                contains("Your OTP is"));
    }

    // ---------- TC-05: OTP SENT VIA MOBILE ----------

    @Test
    void generateOtp_shouldSendMobileOtp_whenAllowed() throws Exception {

        User user = validUser();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(otpRepository.findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
                any(), any(), any()))
                .thenReturn(Optional.empty());

        when(otpRepository.countByUserIdAndOtpTypeAndCreatedAtAfter(
                any(), any(), any()))
                .thenReturn(0L);

        OtpDispatchResult result =
                invokeGenerateOtp(1L, OtpType.MOBILE, OtpReason.REGISTRATION);

        assertEquals(OtpDispatchResult.SENT, result);

        verify(otpRepository).save(any(Otp.class));
        verify(emailService, never()).sendEmail(any(), any(), any());
    }
}
