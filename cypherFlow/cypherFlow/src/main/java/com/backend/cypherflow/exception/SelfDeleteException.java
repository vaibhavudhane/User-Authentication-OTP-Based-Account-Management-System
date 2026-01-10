package com.backend.cypherflow.exception;

public class SelfDeleteException extends RuntimeException {
    public SelfDeleteException(String msg) {
        super(msg);
    }
}

