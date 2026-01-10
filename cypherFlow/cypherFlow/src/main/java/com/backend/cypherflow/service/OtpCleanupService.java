package com.backend.cypherflow.service;

import com.backend.cypherflow.repository.OtpRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OtpCleanupService {

    private final OtpRepository otpRepository;

    public OtpCleanupService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Scheduled(fixedRate = 10 * 60 * 1000) // every 10 minutes
    @Transactional
    public void cleanupOtps() {

        try {
            int deleted = otpRepository.deleteExpiredOtps();

            if (deleted > 0) {
                log.info("OTP cleanup completed. expiredOtpsDeleted={}", deleted);
            } else if (log.isDebugEnabled()) {
                log.debug("OTP cleanup executed. No expired OTPs found");
            }

        } catch (Exception e) {
            log.error("OTP cleanup job failed", e);
            throw e;
        }
    }
}
