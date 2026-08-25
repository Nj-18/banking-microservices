package com.banking.transaction_service.client;

import com.banking.transaction_service.dto.AccountDTO;
import com.banking.transaction_service.dto.TransferRequestDTO;
import com.banking.transaction_service.dto.TransferResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ACCOUNT-SERVICE")
public interface AccountClient {

    @GetMapping("/api/account/{accountNumber}")
    AccountDTO getAccount(@PathVariable String accountNumber);

    @PostMapping("/api/account/transfer")
    TransferResponseDTO transferMoney(@RequestBody TransferRequestDTO request);
}
