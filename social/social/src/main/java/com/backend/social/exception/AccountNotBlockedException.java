package com.backend.social.exception;

public class AccountNotBlockedException extends  RuntimeException{
    public AccountNotBlockedException(String msg) {
        super(msg);
    }
}
