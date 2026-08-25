package com.banking.account_service.exception;

public class SameAccountTransferException extends  RuntimeException{
    public SameAccountTransferException(String message)
    {
        super(message);
    }
}
