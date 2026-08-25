package com.banking.demo.exceptions;

public class AccountInactiveException extends RuntimeException {
    public AccountInactiveException(String s) {
        super(s);
    }
}
