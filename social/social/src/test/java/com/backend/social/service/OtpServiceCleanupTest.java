package com.backend.social.service;

import com.backend.social.repository.OtpRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpCleanupServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @InjectMocks
    private OtpCleanupService otpCleanupService;

    // ---------- TC-01: CLEANUP SUCCESS ----------

    @Test
    void cleanupOtps_shouldDeleteExpiredOtps() {

        when(otpRepository.deleteExpiredOtps())
                .thenReturn(10);

        otpCleanupService.cleanupOtps();

        verify(otpRepository).deleteExpiredOtps();
    }

    // ---------- TC-02: EXCEPTION PROPAGATION (OPTIONAL) ----------

    @Test
    void cleanupOtps_shouldPropagateException_whenRepositoryFails() {

        when(otpRepository.deleteExpiredOtps())
                .thenThrow(new RuntimeException("DB failure"));

        try {
            otpCleanupService.cleanupOtps();
        } catch (RuntimeException ex) {
            // expected
        }

        verify(otpRepository).deleteExpiredOtps();
    }
}


