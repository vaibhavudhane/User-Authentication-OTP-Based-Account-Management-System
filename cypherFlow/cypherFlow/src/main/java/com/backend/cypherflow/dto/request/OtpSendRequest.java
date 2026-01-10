package com.backend.cypherflow.dto.request;

import lombok.Data;
import com.backend.cypherflow.enums.OtpReason;
import com.backend.cypherflow.enums.OtpType;
import jakarta.validation.constraints.NotNull;

@Data
public class OtpSendRequest {

    @NotNull
    private String username;   // needed to identify user safely

    @NotNull
    private OtpType type;      // EMAIL or MOBILE

    @NotNull
    private OtpReason reason;  // REGISTRATION, PASSWORD_RESET, ACCOUNT_UNBLOCK
}

