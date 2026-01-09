package com.backend.social.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "reset token is required")
    private String token;

    @NotBlank(message = "new password is required")
    private String newPassword;
}



