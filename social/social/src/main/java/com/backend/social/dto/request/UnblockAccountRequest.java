package com.backend.social.dto.request;

import com.backend.social.enums.OtpType;
import lombok.Data;

@Data
public class UnblockAccountRequest {
    private String username;
    private OtpType otpType; // EMAIL or MOBILE
}

