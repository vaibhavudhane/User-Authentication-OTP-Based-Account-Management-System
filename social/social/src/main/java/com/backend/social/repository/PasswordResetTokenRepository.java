package com.backend.social.repository;

import com.backend.social.entity.PasswordResetToken;
import com.backend.social.enums.SecurityActionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsedFalse(String tokenHash);
    Optional<PasswordResetToken>
    findByTokenHashAndActionTypeAndActionUsedFalse(
            String tokenHash,
            SecurityActionType actionType
    );


    void deleteByUserId(Long userId);

    void deleteAllByExpiresAtBefore(LocalDateTime now);

    Optional<PasswordResetToken> findByUserId(Long userId);

}

