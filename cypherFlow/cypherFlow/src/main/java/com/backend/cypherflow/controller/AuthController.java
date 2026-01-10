package com.backend.cypherflow.controller;

import com.backend.cypherflow.dto.request.LoginRequest;
import com.backend.cypherflow.dto.request.OtpSendRequest;
import com.backend.cypherflow.dto.request.OtpVerifyRequest;
import com.backend.cypherflow.dto.request.UnblockAccountRequest;
import com.backend.cypherflow.dto.response.AuthResponse;
import com.backend.cypherflow.entity.User;
import com.backend.cypherflow.exception.UserNotFoundException;
import com.backend.cypherflow.repository.UserRepository;
import com.backend.cypherflow.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final LoginService loginService;
    private final OtpService otpService;
    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;
    private final AccountUnblockService accountUnblockService;
    private final OtpVerificationService otpVerificationService;

    public AuthController(
            LoginService loginService,
            OtpService otpService,
            UserRepository userRepository,
            PasswordResetService passwordResetService,
            AccountUnblockService accountUnblockService,
            OtpVerificationService otpVerificationService
    ) {
        this.loginService = loginService;
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.passwordResetService = passwordResetService;
        this.accountUnblockService = accountUnblockService;
        this.otpVerificationService=otpVerificationService;
    }


    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(loginService.login(req));
    }

    // ---------------- BLOCK ACCOUNT ---------------
    @PostMapping("/block-account")
    public ResponseEntity<?> blockAccount(@RequestParam String token) {
        passwordResetService.blockAccount(token);
        return ResponseEntity.ok("Account blocked successfully");
    }


    // ---------------- SEND OTP ----------------
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody @Valid OtpSendRequest req) {

        User user = userRepository.findByUsernameIgnoreCase(req.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        otpService.dispatchOtpAsync(user.getId(), req.getType(), req.getReason());

        return ResponseEntity.ok("OTP sent successfully");
    }

    // ---------------- VERIFY OTP ----------------
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody @Valid OtpVerifyRequest req) {

        User user = userRepository.findByUsernameIgnoreCase(req.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        otpVerificationService.verifyOtp(user, req);

        return ResponseEntity.ok("OTP verified successfully");
    }


    @PostMapping("/unblock-account/verify-otp")
    public ResponseEntity<?> verifyOtpForUnblock(@RequestBody @Valid OtpVerifyRequest req) {
        accountUnblockService.verifyOtpForUnblock(req);
        return ResponseEntity.ok("OTP verified for account unblock");
    }

    // ---------------- UNBLOCK ACCOUNT - SEND OTP ----------------
    @PostMapping("/unblock-account/send-otp")
    public ResponseEntity<?> sendOtpForUnblock(@RequestBody UnblockAccountRequest req) {
        accountUnblockService.sendOtpForUnblock(req);
        return ResponseEntity.ok("OTP sent for account unblock");
    }

}

