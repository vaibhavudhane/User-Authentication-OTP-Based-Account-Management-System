package com.backend.social.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String username;
    private String role;
}

