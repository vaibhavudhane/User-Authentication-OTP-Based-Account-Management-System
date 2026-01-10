package com.backend.cypherflow.service;


import com.backend.cypherflow.dto.request.OtpVerifyRequest;
import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.enums.AccountStatus;
import com.backend.cypherflow.enums.OtpType;
import com.backend.cypherflow.exception.BadRequestException;
import com.backend.cypherflow.exception.UserNotFoundException;
import com.backend.cypherflow.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountVerificationService {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private  final OtpVerificationService otpVerificationService;

    public AccountVerificationService(OtpService otpService,
                                      UserRepository userRepository,
                                      OtpVerificationService otpVerificationService) {
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.otpVerificationService=otpVerificationService;
    }


     //Verify OTP and activate account if both email & mobile are verified
    public void verifyOtpAndActivate(OtpVerifyRequest req) {

        User user = userRepository.findByUsernameIgnoreCase(req.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));


        if (req.getOtpType() == OtpType.EMAIL && user.isEmailVerified()) {
            log.warn(
                    "Email verification attempted but already verified. userId={}",
                    user.getId()
            );
            throw new BadRequestException("Email already verified");
        }


        if (req.getOtpType() == OtpType.MOBILE && user.isMobileVerified()) {
            log.warn(
                    "Mobile verification attempted but already verified. userId={}",
                    user.getId()
            );
            throw new BadRequestException("Mobile already verified");
        }

        if (log.isDebugEnabled()) {
            log.debug(
                    "Verifying registration OTP. userId={}, otpType={}",
                    user.getId(),
                    req.getOtpType()
            );
        }

        // Verify OTP (uses your existing OTP service)
        otpVerificationService.verifyOtp(user, req);

        // Update verification flags
        if (req.getOtpType() == OtpType.EMAIL) {
            user.setEmailVerified(true);
            if (log.isDebugEnabled()) {
                log.debug("Email verified successfully. userId={}", user.getId());
            }
        }

        if (req.getOtpType() == OtpType.MOBILE) {
            user.setMobileVerified(true);
            if (log.isDebugEnabled()) {
                log.debug("Mobile verified successfully. userId={}", user.getId());
            }

        }

        // Activate account ONLY if both verified
        if (user.isEmailVerified() && user.isMobileVerified()) {

            user.setAccountStatus(AccountStatus.ACTIVE);
            user.setFailedLoginAttempts(0);
            user.setLockUntil(null);

            log.info(
                    "Account activated after successful verification. userId={}",
                    user.getId()
            );
        }

        userRepository.save(user);
    }
}

