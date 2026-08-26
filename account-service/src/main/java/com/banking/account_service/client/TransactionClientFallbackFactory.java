package com.banking.account_service.client;

import com.banking.account_service.dto.TransactionRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionClientFallbackFactory implements FallbackFactory<TransactionClient> {

    @Override
    public TransactionClient create(Throwable cause) {
        return new TransactionClient() {
            @Override
            public void recordTransaction(TransactionRequestDTO request) {
                log.error(
                        "Transaction service is unavailable; ledger entry was not recorded. account={}, type={}, ref={}",
                        request.getAccountNumber(),
                        request.getTransactionType(),
                        request.getTransactionReference(),
                        cause);
            }
        };
    }
}
