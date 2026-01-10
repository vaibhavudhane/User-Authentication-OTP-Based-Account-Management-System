package com.backend.cypherflow.repository;

import com.backend.cypherflow.entity.PasswordResetToken;
import com.backend.cypherflow.enums.SecurityActionType;
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

