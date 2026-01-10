package com.backend.cypherflow.service;

import com.backend.cypherflow.dto.request.OtpVerifyRequest;
import com.backend.cypherflow.entity.Otp;
import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.enums.OtpReason;
import com.backend.cypherflow.enums.OtpStatus;
import com.backend.cypherflow.exception.InvalidOtpException;
import com.backend.cypherflow.repository.OtpRepository;
import com.backend.cypherflow.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class OtpVerificationService {

    private static final int MAX_RETRIES = 3;

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;

    public OtpVerificationService(OtpRepository otpRepository, UserRepository userRepository)
    {
        this.otpRepository=otpRepository;
        this.userRepository=userRepository;
    }

    @Transactional
    public void verifyOtp(User user, OtpVerifyRequest request) {

        Otp otp = otpRepository
                .findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(
                        user.getId(), request.getOtpType(), request.getOtpReason()
                )
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP"));

        if (log.isDebugEnabled()) {
            log.debug(
                    "OTP verification attempt. userId={}, otpType={}, reason={}",
                    user.getId(),
                    request.getOtpType(),
                    request.getOtpReason()
            );
        }

        if (otp.getOtpType() != request.getOtpType()) {
            throw new InvalidOtpException("Invalid otp");
        }

        validateOtpState(otp,user.getId());
        validateOtpValue(otp, request.getOtp(), user.getId());
        otp.setStatus(OtpStatus.VERIFIED);
        otpRepository.save(otp);

        if (request.getOtpReason() == OtpReason.PASSWORD_RESET) {
            grantPasswordReset(user);
        }

    }

    /* ================= STATE VALIDATION ================= */

    private void validateOtpState(Otp otp, Long userId) {

        if (otp.getStatus() != OtpStatus.GENERATED) {
            log.warn(
                    "OTP verification failed - invalid status. userId={}, status={}",
                    userId,
                    otp.getStatus()
            );
            throw new InvalidOtpException("OTP already used or blocked");
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepository.save(otp);

            log.warn(
                    "OTP verification failed - OTP expired. userId={}",
                    userId
            );

            throw new InvalidOtpException("OTP expired");
        }
    }

    /* ================= VALUE VALIDATION ================= */

    private void validateOtpValue(Otp otp, String providedOtp, Long userId) {
        if (!BCrypt.checkpw(providedOtp, otp.getOtpHash())) {
            otp.setRetryCount(otp.getRetryCount() + 1);
            if (otp.getRetryCount() >= MAX_RETRIES) {
                otp.setStatus(OtpStatus.BLOCKED);
                log.warn(
                        "OTP blocked after max retry attempts. userId={}",
                        userId
                );
            }
            else {
                log.warn(
                        "Invalid OTP attempt. userId={}, retryCount={}",
                        userId,
                        otp.getRetryCount()
                );
            }
            otpRepository.save(otp);
            throw new InvalidOtpException("Invalid OTP");
        }
    }

    /* ================= PASSWORD RESET ================= */

    private void grantPasswordReset(User user) {
        user.setPasswordResetAllowed(true);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        log.info(
                "Password reset permission granted after OTP verification. userId={}",
                user.getId()
        );

    }
}

