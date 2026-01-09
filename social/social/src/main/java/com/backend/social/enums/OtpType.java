package com.backend.social.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OtpType {

        EMAIL,
        MOBILE;

    @JsonCreator
    public static OtpType fromValue(String value) {
        for (OtpType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid OTP type: " + value);
    }

}

