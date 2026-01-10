package com.backend.cypherflow.dto.request;

import com.backend.cypherflow.enums.OtpType;
import lombok.Data;

@Data
public class UnblockAccountRequest {
    private String username;
    private OtpType otpType; // EMAIL or MOBILE
}

