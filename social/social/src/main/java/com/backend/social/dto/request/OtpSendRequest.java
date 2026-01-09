package com.backend.social.dto.request;

import lombok.Data;
import com.backend.social.enums.OtpReason;
import com.backend.social.enums.OtpType;
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

