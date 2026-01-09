package com.backend.social.exception;

public class CooldownException extends RuntimeException {
    public CooldownException(String message) {
        super(message);
    }
}
