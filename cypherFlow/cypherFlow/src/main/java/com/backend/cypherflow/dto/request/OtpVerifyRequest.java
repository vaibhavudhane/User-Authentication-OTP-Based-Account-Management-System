package com.backend.cypherflow.dto.request;

import com.backend.cypherflow.enums.OtpReason;
import com.backend.cypherflow.enums.OtpType;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OtpVerifyRequest {

    @NotBlank(message = "Username is missing")
    private  String username;

    @NotBlank(message = "OTP is missing")
    @Pattern(regexp = "\\d{6}", message = "OTP must be a 6-digit number")
    private String otp;

    @NotNull(message = "OTP type is missing")
    private OtpType otpType;

    @NotNull(message = "OTP reason is missing")
    private OtpReason otpReason;

}



