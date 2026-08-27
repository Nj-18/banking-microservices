package com.banking.transaction_service.client;

import com.banking.transaction_service.dto.TransferRequestDTO;
import com.banking.transaction_service.exception.DownstreamServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountClientFallbackFactoryTest {

    @Test
    void getAccountFallback_shouldThrowServiceUnavailable() {
        AccountClient fallback = new AccountClientFallbackFactory()
                .create(new RuntimeException("connection refused"));

        DownstreamServiceException ex = assertThrows(
                DownstreamServiceException.class,
                () -> fallback.getAccount("ACC1001"));

        assertEquals(
                "Account service is currently unavailable. Please try again later.",
                ex.getMessage());
    }

    @Test
    void transferFallback_shouldThrowServiceUnavailable() {
        AccountClient fallback = new AccountClientFallbackFactory()
                .create(new RuntimeException("timeout"));

        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(100.0);

        DownstreamServiceException ex = assertThrows(
                DownstreamServiceException.class,
                () -> fallback.transferMoney(request));

        assertEquals(
                "Account service is currently unavailable. Please try again later.",
                ex.getMessage());
    }
}
