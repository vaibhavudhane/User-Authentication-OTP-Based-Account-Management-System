package com.backend.social.repository;

import com.backend.social.entity.Otp;
import com.backend.social.enums.OtpReason;
import com.backend.social.enums.OtpType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByUserIdAndOtpReasonOrderByCreatedAtDesc(
            Long userId,
            OtpReason otpReason

    );

    Optional<Otp> findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
            Long userId,
            OtpType otpType,
            OtpReason otpReason
    );

    //OtpType otpType

    long countByUserIdAndOtpTypeAndCreatedAtAfter(
            Long userId,
            OtpType otpType,
            LocalDateTime createdAt
    );

    @Modifying
    @Transactional
    @Query("""
    DELETE FROM Otp o
    WHERE o.expiresAt < CURRENT_TIMESTAMP""")
    int deleteExpiredOtps();


}



