package com.backend.social.exception;

public class UnverfiedAccountException extends RuntimeException {
    public UnverfiedAccountException(String message)
    {
        super(message);
    }
}
