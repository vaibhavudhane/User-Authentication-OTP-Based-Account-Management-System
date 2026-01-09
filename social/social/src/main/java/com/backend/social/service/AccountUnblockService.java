package com.backend.social.service;

import com.backend.social.dto.request.OtpVerifyRequest;
import com.backend.social.dto.request.UnblockAccountRequest;
import com.backend.social.entity.User;
import com.backend.social.enums.AccountStatus;
import com.backend.social.enums.OtpReason;
import com.backend.social.exception.AccountNotBlockedException;
import com.backend.social.exception.UnauthorizedException;
import com.backend.social.exception.UserNotFoundException;
import com.backend.social.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class AccountUnblockService {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final  OtpVerificationService otpVerificationService;

    public AccountUnblockService(OtpService otpService,
                                 UserRepository userRepository,
                                 EmailService emailService,
                                 OtpVerificationService otpVerificationService)
    {
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.otpVerificationService=otpVerificationService;
    }

    /* ===================== SEND OTP ===================== */


    public void sendOtpForUnblock(UnblockAccountRequest req) {

        User user = userRepository.findByUsernameIgnoreCase(req.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getAccountStatus() != AccountStatus.BLOCKED) {
            log.warn(
                    "Unblock OTP requested for non-blocked account. userId={}",
                    user.getId()
            );
            throw new AccountNotBlockedException("Account is not blocked");
        }

        otpService.dispatchOtpAsync(
                user.getId(),
                req.getOtpType(),
                OtpReason.ACCOUNT_UNBLOCK
        );

        log.info(
                "Account unblock OTP dispatched. userId={}, otpType={}",
                user.getId(),
                req.getOtpType()
        );
    }

    /* ===================== VERIFY OTP & UNBLOCK ===================== */

    @Transactional
    public void verifyOtpForUnblock(OtpVerifyRequest req) {

        if (req.getOtpReason() != OtpReason.ACCOUNT_UNBLOCK) {
            log.warn(
                    "Invalid OTP reason for account unblock attempt. reason={}",
                    req.getOtpReason()
            );
            throw new UnauthorizedException("Invalid OTP reason for unblock");
        }

        User user = userRepository.findByUsernameIgnoreCase(req.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getAccountStatus() != AccountStatus.BLOCKED) {
            log.warn(
                    "OTP verification attempted for non-blocked account. userId={}",
                    user.getId()
            );
            throw new AccountNotBlockedException("Account is not blocked");
        }

        if (log.isDebugEnabled()) {
            log.debug(
                    "Verifying unblock OTP. userId={}, otpReason={}",
                    user.getId(),
                    req.getOtpReason()
            );
        }

        otpVerificationService.verifyOtp(user, req);

        // OTP verified → UNBLOCK
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockUntil(null);

        userRepository.save(user);

        //  Send confirmation email
        emailService.sendAccountUnblockedEmail(user.getEmail());

        log.info(
                "Account unblocked successfully. userId={}",
                user.getId()
        );
    }
}


