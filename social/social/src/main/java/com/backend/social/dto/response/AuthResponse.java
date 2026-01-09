package com.backend.social.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AuthResponse {
    private String token;
    private String username;
}

