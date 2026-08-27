package com.banking.account_service.client;

import com.banking.account_service.dto.TransactionRequestDTO;
import com.banking.account_service.exception.DownstreamServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FeignFallbackFactoryTest {

    @Test
    void customerFallback_shouldThrowServiceUnavailable() {
        CustomerClient fallback = new CustomerClientFallbackFactory()
                .create(new RuntimeException("connection refused"));

        DownstreamServiceException ex = assertThrows(
                DownstreamServiceException.class,
                () -> fallback.getCustomer(10L));

        assertEquals(
                "Customer service is currently unavailable. Please try again later.",
                ex.getMessage());
    }

    @Test
    void transactionFallback_shouldSwallowLedgerFailures() {
        TransactionClient fallback = new TransactionClientFallbackFactory()
                .create(new RuntimeException("timeout"));

        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAccountNumber("ACC1001");
        request.setTransactionType("DEPOSIT");
        request.setTransactionReference("TXN1");

        assertDoesNotThrow(() -> fallback.recordTransaction(request));
    }
}
