package com.backend.social.service;

import com.backend.social.dto.response.AuthResponse;
import com.backend.social.dto.request.LoginRequest;
import com.backend.social.entity.User;
import com.backend.social.enums.AccountStatus;
import com.backend.social.exception.UnauthorizedException;
import com.backend.social.exception.UnverfiedAccountException;
import com.backend.social.repository.UserRepository;
import com.backend.social.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class LoginService {

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int LOCK_DURATION_HOURS = 24;


    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public LoginService(UserRepository userRepo, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(LoginRequest req) {

        User user = userRepo.findByUsernameIgnoreCase(req.getUsername())
                .orElse(null);

        // Username check
        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Long userId = user.getId();

        if (log.isDebugEnabled()) {
            log.debug("Login attempt received. userId={}", userId);
        }

        /* ================= ACCOUNT STATUS CHECKS ================= */

        if (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            log.warn("Login blocked - account pending verification. userId={}", userId);
            throw new UnverfiedAccountException("Verify account before login");
        }

        if (user.getAccountStatus() == AccountStatus.BLOCKED) {
            log.warn("Login blocked - account permanently blocked. userId={}", userId);
            throw new UnauthorizedException("Account blocked");
        }

        if (user.getAccountStatus() == AccountStatus.LOCKED &&
                user.getLockUntil() != null &&
                user.getLockUntil().isAfter(LocalDateTime.now()))
        {
            log.warn("Login blocked - account temporarily locked. userId={}", userId);
            throw new UnauthorizedException("Account locked");
        }

        // Auto-unlock after lock expiry
        if (user.getAccountStatus() == AccountStatus.LOCKED &&
                user.getLockUntil() != null &&
                user.getLockUntil().isBefore(LocalDateTime.now()))
        {

            user.setAccountStatus(AccountStatus.ACTIVE);
            user.setFailedLoginAttempts(0);
            user.setLockUntil(null);

            log.info("Account auto-unlocked after lock expiry. userId={}", userId);
        }


        /* ================= PASSWORD CHECK ================= */

        if (!encoder.matches(req.getPassword(), user.getPasswordHash()))
        {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            if (user.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
                user.setAccountStatus(AccountStatus.LOCKED);
                user.setLockUntil(LocalDateTime.now().plusHours(LOCK_DURATION_HOURS));

                log.warn(
                        "Account locked due to repeated failed login attempts. userId={}",
                        userId
                );
            }

            userRepo.save(user);
            throw new UnauthorizedException("Invalid credentials");
        }

        /* ================= SUCCESS ================= */

        user.setFailedLoginAttempts(0);
        user.setLockUntil(null);
        userRepo.save(user);

        log.info("Login successful. userId={}", userId);

        return AuthResponse.builder()
                .username(user.getUsername())
                .token(jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole()))
                .build();
    }
}


