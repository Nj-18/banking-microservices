package com.banking.account_service.controller;

import com.banking.account_service.dto.*;
import com.banking.account_service.entity.BankAccount;
import com.banking.account_service.serviceImpl.BankAccountServiceImpl;
import com.banking.account_service.service.BankAccountService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping
    public BankAccount createAccount(
            @RequestBody CreateBankAccountRequestDTO requestDTO) {

        return bankAccountService.createAccount(requestDTO);
    }

    @PostMapping("/deposit")
    public DepositResponseDTO depositMoney(
            @RequestBody DepositRequestDTO request) {

        return bankAccountService.depositMoney(request);
    }

    @PostMapping("/withdraw")
    public WithdrawResponseDTO withdrawMoney(
            @RequestBody WithdrawRequestDTO request) {

        return bankAccountService.withdrawMoney(request);
    }

    @PostMapping("/transfer")
    public TransferResponseDTO transferMoney(
            @RequestBody TransferRequestDTO request) {

        return bankAccountService.transferMoney(request);
    }
}