package com.backend.social.service;

import com.backend.social.dto.request.LoginRequest;
import com.backend.social.dto.response.AuthResponse;
import com.backend.social.entity.User;
import com.backend.social.enums.AccountStatus;
import com.backend.social.enums.Role;
import com.backend.social.exception.UnauthorizedException;
import com.backend.social.exception.UnverfiedAccountException;
import com.backend.social.repository.UserRepository;
import com.backend.social.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private LoginService loginService;

    // ---------- TEST HELPERS ----------

    private LoginRequest validLoginRequest() {
        LoginRequest req = new LoginRequest();
        req.setUsername("vaibhav");
        req.setPassword("password");
        return req;
    }

    private User activeUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("vaibhav");
        user.setPasswordHash("encodedPass");
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setRole(Role.USER);
        user.setFailedLoginAttempts(0);
        return user;
    }

    // ---------- TC-01: SUCCESSFUL LOGIN ----------

    @Test
    void login_shouldSucceed_whenCredentialsAreValid() {

        LoginRequest req = validLoginRequest();
        User user = activeUser();

        when(userRepo.findByUsernameIgnoreCase("vaibhav"))
                .thenReturn(Optional.of(user));

        when(encoder.matches("password", "encodedPass"))
                .thenReturn(true);

        when(jwtUtil.generateToken(anyLong(), anyString(), any()))
                .thenReturn("jwt-token");

        AuthResponse response = loginService.login(req);

        assertNotNull(response);
        assertEquals("vaibhav", response.getUsername());
        assertEquals("jwt-token", response.getToken());

        verify(userRepo).save(user);
    }

    // ---------- TC-02: LOCKED BUT EXPIRED ----------

    @Test
    void login_shouldUnlockAndLogin_whenLockExpired() {

        LoginRequest req = validLoginRequest();
        User user = activeUser();
        user.setAccountStatus(AccountStatus.LOCKED);
        user.setLockUntil(LocalDateTime.now().minusHours(1));

        when(userRepo.findByUsernameIgnoreCase("vaibhav"))
                .thenReturn(Optional.of(user));

        when(encoder.matches(any(), any()))
                .thenReturn(true);

        when(jwtUtil.generateToken(anyLong(), anyString(), any()))
                .thenReturn("jwt");

        AuthResponse response = loginService.login(req);

        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNotNull(response);
    }

    // ---------- TC-03: USER NOT FOUND ----------

    @Test
    void login_shouldThrowException_whenUserNotFound() {

        when(userRepo.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> loginService.login(validLoginRequest()));
    }

    // ---------- TC-04: PENDING VERIFICATION ----------

    @Test
    void login_shouldThrowException_whenAccountPendingVerification() {

        User user = activeUser();
        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);

        when(userRepo.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        assertThrows(UnverfiedAccountException.class,
                () -> loginService.login(validLoginRequest()));
    }

    // ---------- TC-05: BLOCKED ACCOUNT ----------

    @Test
    void login_shouldThrowException_whenAccountBlocked() {

        User user = activeUser();
        user.setAccountStatus(AccountStatus.BLOCKED);

        when(userRepo.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class,
                () -> loginService.login(validLoginRequest()));
    }

    // ---------- TC-06: LOCKED & NOT EXPIRED ----------

    @Test
    void login_shouldThrowException_whenAccountLocked() {

        User user = activeUser();
        user.setAccountStatus(AccountStatus.LOCKED);
        user.setLockUntil(LocalDateTime.now().plusHours(2));

        when(userRepo.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class,
                () -> loginService.login(validLoginRequest()));
    }

    // ---------- TC-07: WRONG PASSWORD (INCREMENT ATTEMPTS) ----------

    @Test
    void login_shouldIncrementFailedAttempts_whenPasswordIsWrong() {

        User user = activeUser();
        user.setFailedLoginAttempts(1);

        when(userRepo.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        when(encoder.matches(any(), any()))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> loginService.login(validLoginRequest()));

        assertEquals(2, user.getFailedLoginAttempts());
        verify(userRepo).save(user);
    }

    // ---------- TC-08: WRONG PASSWORD → ACCOUNT LOCK ----------

    @Test
    void login_shouldLockAccount_whenMaxAttemptsReached() {

        User user = activeUser();
        user.setFailedLoginAttempts(2);

        when(userRepo.findByUsernameIgnoreCase(any()))
                .thenReturn(Optional.of(user));

        when(encoder.matches(any(), any()))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> loginService.login(validLoginRequest()));

        assertEquals(AccountStatus.LOCKED, user.getAccountStatus());
        assertNotNull(user.getLockUntil());
        verify(userRepo).save(user);
    }
}
