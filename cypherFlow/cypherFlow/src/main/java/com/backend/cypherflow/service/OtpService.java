package com.backend.cypherflow.service;

import com.backend.cypherflow.entity.Otp;
import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.enums.OtpReason;
import com.backend.cypherflow.enums.OtpStatus;
import com.backend.cypherflow.enums.OtpType;
import com.backend.cypherflow.enums.OtpDispatchResult;
import com.backend.cypherflow.exception.UserNotFoundException;
import com.backend.cypherflow.repository.OtpRepository;
import com.backend.cypherflow.repository.UserRepository;
import com.backend.cypherflow.util.OtpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class OtpService {

    private static final int MAX_PER_HOUR = 5;
    private static final int COOLDOWN_SECONDS = 30;


    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public OtpService(OtpRepository otpRepository,
                      EmailService emailService,
                      UserRepository userRepository)
    {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    /* ================= ASYNC ENTRY ================= */

    @Async("otpExecutor")
    public void dispatchOtpAsync(Long userId, OtpType type, OtpReason reason) {

         try {
                if (log.isDebugEnabled()) {
                log.debug(
                        "OTP dispatch requested. userId={}, type={}, reason={}",
                        userId, type, reason
                );
            }

        OtpDispatchResult result = generateAndSendOtp(userId, type, reason);

        if (result == OtpDispatchResult.COOLDOWN || result == OtpDispatchResult.RATE_LIMITED) {
            log.warn(
                    "OTP dispatch blocked. result={}, userId={}, type={}",
                    result, userId, type
            );
        }

    } catch (Exception ex) {
        log.error(
                "OTP async dispatch failed. userId={}, type={}, reason={}",
                userId, type, reason, ex
        );
    }
}


    /* ================= CORE LOGIC ================= */

    private OtpDispatchResult generateAndSendOtp(Long userId, OtpType type, OtpReason reason) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        if (isCooldownActive(userId, type, reason, now)) {
            return OtpDispatchResult.COOLDOWN;
        }

        if (isRateLimitExceeded(userId, type, now)) {
            return OtpDispatchResult.RATE_LIMITED;
        }

        String otpValue = OtpUtil.generateOtp();

        Otp otp = Otp.builder()
                .userId(userId)
                .otpHash(BCrypt.hashpw(otpValue, BCrypt.gensalt()))
                .otpType(type)
                .otpReason(reason)
                .status(OtpStatus.GENERATED)
                .expiresAt(now.plusMinutes(10))
                .lastSentAt(now)
                .retryCount(0)
                .build();

        otpRepository.save(otp);

        sendOtp(user, type, otpValue);

        return OtpDispatchResult.SENT;
    }

    /* ================= HELPERS ================= */

    private boolean isCooldownActive(Long userId, OtpType type, OtpReason reason, LocalDateTime now) {
        return otpRepository
                .findTopByUserIdAndOtpTypeAndOtpReasonOrderByCreatedAtDesc(userId, type, reason)
                .filter(last ->
                        last.getLastSentAt() != null &&
                                last.getLastSentAt().plusSeconds(COOLDOWN_SECONDS).isAfter(now))
                .isPresent();
    }


    private boolean isRateLimitExceeded(Long userId, OtpType type, LocalDateTime now) {
        return otpRepository.countByUserIdAndOtpTypeAndCreatedAtAfter(
                userId, type, now.minusHours(1)
        ) >= MAX_PER_HOUR;
    }

    private void sendOtp(User user, OtpType type, String otp) {
        if (type == OtpType.EMAIL) {
            emailService.sendEmail(
                    user.getEmail(),
                    "OTP Verification",
                    "Your OTP is: " + otp
            );
        } else {

            log.warn("DEV MODE OTP mobile={} otp={}", user.getMobile(), otp);
//            if (log.isDebugEnabled()) {
//                log.debug("OTP generated for mobile delivery. userId={}", user.getId());
//            }
        }
    }
}






