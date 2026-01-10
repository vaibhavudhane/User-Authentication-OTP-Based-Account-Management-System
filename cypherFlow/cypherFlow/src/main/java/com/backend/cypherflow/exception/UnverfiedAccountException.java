package com.backend.cypherflow.exception;

public class UnverfiedAccountException extends RuntimeException {
    public UnverfiedAccountException(String message)
    {
        super(message);
    }
}
