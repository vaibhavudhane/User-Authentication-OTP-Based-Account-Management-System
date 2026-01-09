package com.backend.social.exception;

public class SelfDeleteException extends RuntimeException {
    public SelfDeleteException(String msg) {
        super(msg);
    }
}

