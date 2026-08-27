package com.banking.transaction_service.client;

import com.banking.transaction_service.dto.AccountDTO;
import com.banking.transaction_service.dto.TransferRequestDTO;
import com.banking.transaction_service.dto.TransferResponseDTO;
import com.banking.transaction_service.exception.DownstreamServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountClientFallbackFactory implements FallbackFactory<AccountClient> {

    @Override
    public AccountClient create(Throwable cause) {
        return new AccountClient() {
            @Override
            public AccountDTO getAccount(String accountNumber) {
                log.error("Account service is unavailable while fetching account {}", accountNumber, cause);
                throw new DownstreamServiceException(
                        "Account service is currently unavailable. Please try again later.");
            }

            @Override
            public TransferResponseDTO transferMoney(TransferRequestDTO request) {
                log.error(
                        "Account service is unavailable while transferring from {} to {}",
                        request.getFromAccountNumber(),
                        request.getToAccountNumber(),
                        cause);
                throw new DownstreamServiceException(
                        "Account service is currently unavailable. Please try again later.");
            }
        };
    }
}
