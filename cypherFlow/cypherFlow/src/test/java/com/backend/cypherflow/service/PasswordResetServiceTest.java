package com.backend.cypherflow.service;


import com.backend.cypherflow.util.AccountStatusValidator;
import com.backend.cypherflow.dto.request.ResetPasswordRequest;
import com.backend.cypherflow.entity.PasswordResetToken;
import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.enums.AccountStatus;
import com.backend.cypherflow.enums.SecurityActionType;
import com.backend.cypherflow.exception.CooldownException;
import com.backend.cypherflow.exception.InvalidPasswordException;
import com.backend.cypherflow.exception.InvalidTokenException;
import com.backend.cypherflow.repository.PasswordResetTokenRepository;
import com.backend.cypherflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private AccountStatusValidator accountStatusValidator;

    @InjectMocks
    private PasswordResetService passwordResetService;

    // ---------- HELPERS ----------

    private User validUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        return user;
    }

    private PasswordResetToken validToken(User user) {
        return PasswordResetToken.builder()
                .user(user)
                .tokenHash("hashed-token")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();
    }

    // ================= FORGOT PASSWORD =================

    @Test
    void initiateForgotPassword_shouldDoNothing_whenUserNotFound() {

        when(userRepository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.empty());

        passwordResetService.initiateForgotPassword("x@gmail.com");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void initiateForgotPassword_shouldThrowCooldownException_whenCooldownActive() {

        User user = validUser();

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .firstRequestAt(LocalDateTime.now())
                .lastRequestAt(LocalDateTime.now())
                .requestCount(1)
                .build();

        when(userRepository.findByEmailIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        when(tokenRepository.findByUserId(user.getId()))
                .thenReturn(Optional.of(token));

        assertThrows(CooldownException.class,
                () -> passwordResetService.initiateForgotPassword(user.getEmail()));
    }

    // ================= RESET PASSWORD =================

    @Test
    void resetPassword_shouldThrowInvalidToken_whenTokenNotFound() {

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("bad-token");

        when(tokenRepository.findByTokenHashAndUsedFalse(any()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class,
                () -> passwordResetService.resetPassword(req));
    }

    @Test
    void resetPassword_shouldThrowInvalidPassword_whenPasswordBlank() {

        User user = validUser();
        PasswordResetToken token = validToken(user);

        when(tokenRepository.findByTokenHashAndUsedFalse(any()))
                .thenReturn(Optional.of(token));

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("token");
        req.setNewPassword(" ");

        assertThrows(InvalidPasswordException.class,
                () -> passwordResetService.resetPassword(req));
    }

    @Test
    void resetPassword_shouldSucceed_whenValid() {

        User user = validUser();
        PasswordResetToken token = validToken(user);

        when(tokenRepository.findByTokenHashAndUsedFalse(any()))
                .thenReturn(Optional.of(token));

        when(passwordEncoder.encode(any()))
                .thenReturn("encoded");

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("token");
        req.setNewPassword("newPass");

        passwordResetService.resetPassword(req);

        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
        verify(emailService).sendPasswordResetAlertEmail(any(), any());
    }

    // ================= BLOCK ACCOUNT =================

    @Test
    void blockAccount_shouldBlockUser_whenTokenValid() {

        User user = validUser();

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .actionUsed(false)
                .actionType(SecurityActionType.ACCOUNT_BLOCK)
                .build();

        when(tokenRepository.findByTokenHashAndActionTypeAndActionUsedFalse(
                any(), eq(SecurityActionType.ACCOUNT_BLOCK)))
                .thenReturn(Optional.of(token));

        passwordResetService.blockAccount("raw-token");

        assertEquals(AccountStatus.BLOCKED, user.getAccountStatus());
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }
}

