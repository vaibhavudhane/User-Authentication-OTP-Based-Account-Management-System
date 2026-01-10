package com.backend.cypherflow.exception;

public class AccountNotBlockedException extends  RuntimeException{
    public AccountNotBlockedException(String msg) {
        super(msg);
    }
}
