package com.backend.social.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class ForgotUsernameRequest {

    @NotBlank(message = "email is required")
    private String email;

}
