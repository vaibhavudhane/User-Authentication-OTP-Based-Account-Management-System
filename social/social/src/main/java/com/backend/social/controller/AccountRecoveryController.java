package com.backend.social.controller;

import com.backend.social.dto.request.ForgotPasswordRequest;
import com.backend.social.dto.request.ForgotUsernameRequest;
import com.backend.social.dto.request.ResetPasswordRequest;
import com.backend.social.service.ForgotUsernameService;
import com.backend.social.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")

public class AccountRecoveryController {

    private final ForgotUsernameService forgotUsernameService;
    private final PasswordResetService passwordResetService;

    public AccountRecoveryController(ForgotUsernameService forgotUsernameService,
                                     PasswordResetService passwordResetService)
    {
        this.forgotUsernameService = forgotUsernameService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-username")
    public ResponseEntity<?> forgotUsername(@Valid @RequestBody ForgotUsernameRequest request) {
        forgotUsernameService.forgotUsername(request);
        return ResponseEntity.ok("If the account is active, the username has been sent to the registered email");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.initiateForgotPassword(request.getEmail());
        return ResponseEntity.ok("If email exists, reset link sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok("Password reset successful");
    }
}
