package com.backend.social.service;

import com.backend.social.dto.request.OtpVerifyRequest;
import com.backend.social.dto.request.UnblockAccountRequest;
import com.backend.social.entity.User;
import com.backend.social.enums.AccountStatus;
import com.backend.social.enums.OtpReason;
import com.backend.social.enums.OtpType;
import com.backend.social.exception.AccountNotBlockedException;
import com.backend.social.exception.UnauthorizedException;
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
class AccountUnblockServiceTest {

    @Mock
    private OtpService otpService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpVerificationService otpVerificationService;

    @InjectMocks
    private AccountUnblockService accountUnblockService;

    // ---------- HELPERS ----------

    private User blockedUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("vaibhav");
        user.setEmail("test@gmail.com");
        user.setAccountStatus(AccountStatus.BLOCKED);
        user.setFailedLoginAttempts(3);
        user.setLockUntil(null);
        return user;
    }

    private UnblockAccountRequest unblockReq() {
        UnblockAccountRequest req = new UnblockAccountRequest();
        req.setUsername("vaibhav");
        req.setOtpType(OtpType.EMAIL);
        return req;
    }

    private OtpVerifyRequest verifyReq() {
        OtpVerifyRequest req = new OtpVerifyRequest();
        req.setUsername("vaibhav");
        req.setOtpType(OtpType.EMAIL);
        req.setOtpReason(OtpReason.ACCOUNT_UNBLOCK);
        return req;
    }

    // ---------- AU-01: USER NOT FOUND (SEND OTP) ----------

    @Test
    void sendOtpForUnblock_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> accountUnblockService.sendOtpForUnblock(unblockReq()));
    }

    // ---------- AU-02: ACCOUNT NOT BLOCKED ----------

    @Test
    void sendOtpForUnblock_shouldThrowException_whenAccountNotBlocked() {

        User user = blockedUser();
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        assertThrows(AccountNotBlockedException.class,
                () -> accountUnblockService.sendOtpForUnblock(unblockReq()));

        verify(otpService, never()).dispatchOtpAsync(any(), any(), any());
    }

    // ---------- AU-03: OTP DISPATCHED ----------

    @Test
    void sendOtpForUnblock_shouldDispatchOtp_whenAccountBlocked() {

        User user = blockedUser();

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        accountUnblockService.sendOtpForUnblock(unblockReq());

        verify(otpService).dispatchOtpAsync(
                eq(1L),
                eq(OtpType.EMAIL),
                eq(OtpReason.ACCOUNT_UNBLOCK)
        );
    }

    // ---------- AU-04: INVALID OTP REASON ----------

    @Test
    void verifyOtpForUnblock_shouldThrowException_whenInvalidOtpReason() {

        OtpVerifyRequest req = verifyReq();
        req.setOtpReason(OtpReason.REGISTRATION);

        assertThrows(UnauthorizedException.class,
                () -> accountUnblockService.verifyOtpForUnblock(req));
    }

    // ---------- AU-05: USER NOT FOUND (VERIFY OTP) ----------

    @Test
    void verifyOtpForUnblock_shouldThrowException_whenUserNotFound() {

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> accountUnblockService.verifyOtpForUnblock(verifyReq()));
    }

    // ---------- AU-06: ACCOUNT NOT BLOCKED (VERIFY OTP) ----------

    @Test
    void verifyOtpForUnblock_shouldThrowException_whenAccountNotBlocked() {

        User user = blockedUser();
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        assertThrows(AccountNotBlockedException.class,
                () -> accountUnblockService.verifyOtpForUnblock(verifyReq()));

        verify(otpVerificationService, never()).verifyOtp(any(), any());
    }

    // ---------- AU-07: SUCCESSFUL UNBLOCK ----------

    @Test
    void verifyOtpForUnblock_shouldUnblockAccount_whenOtpValid() {

        User user = blockedUser();

        when(userRepository.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        doNothing().when(otpVerificationService).verifyOtp(any(), any());

        accountUnblockService.verifyOtpForUnblock(verifyReq());

        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockUntil());

        verify(userRepository).save(user);
        verify(emailService).sendAccountUnblockedEmail("test@gmail.com");
    }
}

