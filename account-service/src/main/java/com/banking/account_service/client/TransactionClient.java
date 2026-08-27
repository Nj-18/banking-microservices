package com.banking.account_service.client;

import com.banking.account_service.dto.TransactionRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "TRANSACTION-SERVICE", fallbackFactory = TransactionClientFallbackFactory.class)
public interface TransactionClient {

    @PostMapping("/api/transactions")
    void recordTransaction(@RequestBody TransactionRequestDTO request);
}
