package com.backend.social.exception;

public class InvalidContentException extends RuntimeException {
    public InvalidContentException(String message) {
        super(message);
    }
}
