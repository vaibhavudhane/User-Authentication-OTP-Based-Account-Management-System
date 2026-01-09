package com.backend.social.service;

import com.backend.social.dto.request.OtpVerifyRequest;
import com.backend.social.entity.User;
import com.backend.social.enums.AccountStatus;
import com.backend.social.enums.OtpType;
import com.backend.social.exception.BadRequestException;
import com.backend.social.exception.UserNotFoundException;
import com.backend.social.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountVerificationServiceTest {

    @Mock
    private OtpService otpService; // not used directly

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpVerificationService otpVerificationService;

    @InjectMocks
    private AccountVerificationService accountVerificationService;

    // ---------- HELPERS ----------

    private User baseUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("vaibhav");
        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.setMobileVerified(false);
        return user;
    }

    private OtpVerifyRequest req(OtpType type) {
        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setUsername("vaibhav");
        req.setOtpType(type);
        return req;
    }

    // ---------- AV-01: USER NOT FOUND ----------

    @Test
    void verifyOtpAndActivate_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> accountVerificationService.verifyOtpAndActivate(req(OtpType.EMAIL)));
    }

    // ---------- AV-02: EMAIL ALREADY VERIFIED ----------

    @Test
    void verifyOtpAndActivate_shouldThrowException_whenEmailAlreadyVerified() {

        User user = baseUser();
        user.setEmailVerified(true);

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> accountVerificationService.verifyOtpAndActivate(req(OtpType.EMAIL)));

        verify(otpVerificationService, never()).verifyOtp(any(), any());
    }

    // ---------- AV-03: MOBILE ALREADY VERIFIED ----------

    @Test
    void verifyOtpAndActivate_shouldThrowException_whenMobileAlreadyVerified() {

        User user = baseUser();
        user.setMobileVerified(true);

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> accountVerificationService.verifyOtpAndActivate(req(OtpType.MOBILE)));

        verify(otpVerificationService, never()).verifyOtp(any(), any());
    }

    // ---------- AV-04: VERIFY EMAIL ONLY ----------

    @Test
    void verifyOtpAndActivate_shouldVerifyEmailOnly() {

        User user = baseUser();

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        doNothing().when(otpVerificationService).verifyOtp(any(), any());

        accountVerificationService.verifyOtpAndActivate(req(OtpType.EMAIL));

        assertTrue(user.isEmailVerified());
        assertFalse(user.isMobileVerified());
        assertEquals(AccountStatus.PENDING_VERIFICATION, user.getAccountStatus());

        verify(userRepository).save(user);
    }

    // ---------- AV-05: VERIFY MOBILE ONLY ----------

    @Test
    void verifyOtpAndActivate_shouldVerifyMobileOnly() {

        User user = baseUser();

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        doNothing().when(otpVerificationService).verifyOtp(any(), any());

        accountVerificationService.verifyOtpAndActivate(req(OtpType.MOBILE));

        assertTrue(user.isMobileVerified());
        assertFalse(user.isEmailVerified());
        assertEquals(AccountStatus.PENDING_VERIFICATION, user.getAccountStatus());

        verify(userRepository).save(user);
    }

    // ---------- AV-06: VERIFY SECOND OTP → ACTIVATE ----------

    @Test
    void verifyOtpAndActivate_shouldActivateAccount_whenBothVerified() {

        User user = baseUser();
        user.setEmailVerified(true); // already verified email

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        doNothing().when(otpVerificationService).verifyOtp(any(), any());

        accountVerificationService.verifyOtpAndActivate(req(OtpType.MOBILE));

        assertTrue(user.isEmailVerified());
        assertTrue(user.isMobileVerified());
        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockUntil());

        verify(userRepository).save(user);
    }
}

