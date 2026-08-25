package com.banking.account_service.exception;

public class AccountInactiveException extends RuntimeException {
    public AccountInactiveException(String s) {
        super(s);
    }
}
