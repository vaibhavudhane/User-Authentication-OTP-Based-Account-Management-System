package com.backend.social.controller;

import com.backend.social.dto.request.OtpVerifyRequest;
import com.backend.social.dto.request.RegisterRequest;
import com.backend.social.enums.OtpReason;
import com.backend.social.enums.OtpType;
import com.backend.social.service.AccountVerificationService;
import com.backend.social.service.OtpService;
import com.backend.social.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final OtpService otpService;
    private final AccountVerificationService accountVerificationService;

    public RegistrationController(RegistrationService registrationService,
                                  OtpService otpService,
                                  AccountVerificationService accountVerificationService)
    {
        this.registrationService = registrationService;
        this.otpService = otpService;
        this.accountVerificationService=accountVerificationService;
    }


    // ---------------- REGISTER ----------------
    @PostMapping("/register")
    public ResponseEntity<?>  register( @Valid @RequestBody  RegisterRequest req) {

        Long userId = registrationService.registerOrReuse(req);

        otpService.dispatchOtpAsync(userId, OtpType.EMAIL, OtpReason.REGISTRATION);
        otpService.dispatchOtpAsync(userId, OtpType.MOBILE, OtpReason.REGISTRATION);
        return ResponseEntity.ok("Registered successfully. Verify OTP for email and mobile to activate your account.");
    }

    // ---------------- VERIFY REGISTRATION OTP ---------------
    @PostMapping("/verify-registration-otp")
    public ResponseEntity<?> verifyRegistrationOtp(@Valid @RequestBody OtpVerifyRequest req) {
        accountVerificationService.verifyOtpAndActivate(req);
        return ResponseEntity.ok("OTP verified successfully");
    }
}
