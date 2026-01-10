package com.backend.cypherflow.service;

import com.backend.cypherflow.util.AccountStatusValidator;
import com.backend.cypherflow.dto.request.ResetPasswordRequest;
import com.backend.cypherflow.entity.PasswordResetToken;
import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.enums.AccountStatus;
import com.backend.cypherflow.enums.SecurityActionType;
import com.backend.cypherflow.exception.*;
import com.backend.cypherflow.repository.PasswordResetTokenRepository;
import com.backend.cypherflow.repository.UserRepository;
import com.backend.cypherflow.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    private static final int MAX_REQUESTS_PER_HOUR = 5;
    private static final int COOLDOWN_SECONDS = 30;


    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AccountStatusValidator accountStatusValidator;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService,
                                AccountStatusValidator accountStatusValidator)
    {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.accountStatusValidator=accountStatusValidator;
    }

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordUrl;

    /* ================= FORGOT PASSWORD ================= */

    @Transactional
    public void initiateForgotPassword(String email) {

        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {

            accountStatusValidator.validate(user);

            LocalDateTime now = LocalDateTime.now();

            PasswordResetToken existing =
                    tokenRepository.findByUserId(user.getId()).orElse(null);

            // ============ FIRST REQUEST ============
            if (existing == null || windowExpired(existing, now)) {
                resetAndCreate(user, now, 1);
                return;

            }

            if (cooldownActive(existing, now)) {
                log.warn(
                        "Password reset cooldown violation. userId={}",
                        user.getId()
                );
                throw new CooldownException("Please wait for 30 sec before requesting another reset");
            }

            if (existing.getRequestCount() >= MAX_REQUESTS_PER_HOUR) {
                log.warn(
                        "Password reset rate limit exceeded. userId={}",
                        user.getId()
                );
                throw new LimitExceededException("Too many requests. Try again later");
            }

            // ============ ALLOWED ============
            tokenRepository.delete(existing);
            resetAndCreate(user, now, existing.getRequestCount() + 1);

        });
    }

    private boolean cooldownActive(PasswordResetToken token, LocalDateTime now) {
        return token.getLastRequestAt()
                .plusSeconds(COOLDOWN_SECONDS)
                .isAfter(now);
    }

    private boolean windowExpired(PasswordResetToken token, LocalDateTime now) {
        return token.getFirstRequestAt()
                .plusHours(1)
                .isBefore(now);
    }

    private void resetAndCreate(User user, LocalDateTime now, int count) {

        String rawToken = TokenUtil.generateToken();
        String tokenHash = TokenUtil.hashToken(rawToken);

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(now.plusMinutes(30))
                .requestCount(count)
                .firstRequestAt(now)
                .lastRequestAt(now)
                .used(false)
                .createdAt(now)
                .build();

        tokenRepository.save(token);

        String link = resetPasswordUrl + "?token=" + rawToken;
        emailService.sendPasswordResetEmail(user.getEmail(), link);

        if (log.isDebugEnabled()) {
            log.debug("Password reset token generated and email dispatched. userId={}", user.getId());
        }
    }


    /* ================= RESET PASSWORD ================= */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {


        String tokenHash = TokenUtil.hashToken(request.getToken());

        PasswordResetToken resetToken = tokenRepository
                .findByTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Invalid password reset token used");
                    return new InvalidTokenException("Invalid or expired token");
                });

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Expired password reset token used. userId={}",
                    resetToken.getUser().getId());
            throw new TokenExpiredException("Reset token expired");
        }

        User user = resetToken.getUser();
        accountStatusValidator.validate(user);


        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new InvalidPasswordException("Password cannot be empty");
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        if (encodedPassword == null) {
            throw new InvalidPasswordException("Password encoding failed");
        }

        user.setPasswordHash(encodedPassword);
        userRepository.save(user);
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset completed successfully. userId={}", user.getId());

        //SEND SECURITY ALERT MAIL
        sendPasswordResetSecurityMail(user);

    }

    /* ================= SECURITY ALERT ================= */
    private void sendPasswordResetSecurityMail(User user) {

        String rawToken = TokenUtil.generateToken();

        String tokenHash = TokenUtil.hashToken(rawToken);

        PasswordResetToken securityToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .actionType(SecurityActionType.ACCOUNT_BLOCK)
                .actionUsed(false)
                .createdAt(LocalDateTime.now())
                .build();

        tokenRepository.save(securityToken);
//"http://localhost:8080/auth/block-account?token="
        String blockLink = resetPasswordUrl + rawToken;

        emailService.sendPasswordResetAlertEmail(user.getEmail(), blockLink);

    }

    /* ================= BLOCK ACCOUNT ================= */

    @Transactional
    public void blockAccount(String rawToken) {

        String tokenHash = TokenUtil.hashToken(rawToken);

        PasswordResetToken token = tokenRepository
                .findByTokenHashAndActionTypeAndActionUsedFalse(
                        tokenHash,
                        SecurityActionType.ACCOUNT_BLOCK
                )
                .orElseThrow(() -> {
                    log.warn("Invalid account block token used");
                    return new InvalidTokenException("Invalid or expired action token");
                });


        if (token.isActionUsed()) {
            log.warn("already token used. userId={}",
                    token.getUser().getId());
            throw new TokenExpiredException("Action already performed");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Expired account block token used. userId={}",
                    token.getUser().getId());
            throw new TokenExpiredException("Token expired");
        }

        User user = token.getUser();

        user.setAccountStatus(AccountStatus.BLOCKED);
        userRepository.save(user);
        token.setActionUsed(true);
        tokenRepository.save(token);

        log.info("Account blocked via security action link. userId={}", user.getId());
    }


}
