package com.backend.cypherflow.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OtpReason {
    REGISTRATION,
    PASSWORD_RESET,
    ACCOUNT_UNBLOCK,
    LOGIN;

    @JsonCreator
    public static OtpReason fromValue(String value) {
        for (OtpReason type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid OTP Reason: " + value);
    }
}



