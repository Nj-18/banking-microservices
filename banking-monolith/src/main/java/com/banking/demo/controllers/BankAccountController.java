package com.banking.demo.controllers;

import com.banking.demo.dtos.*;
import com.banking.demo.entities.BankAccount;
import com.banking.demo.entities.Customer;
import com.banking.demo.serviceImpl.BankAccountServiceImpl;
import com.banking.demo.services.BankAccountService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountServiceImpl bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping("/")
    public BankAccount createAccount(@RequestBody CreateBankAccountRequestDTO requestDTO) {

        return bankAccountService.createAccount(requestDTO);

    }

    @PostMapping("/deposit")
    public DepositResponseDTO depositMoney(@RequestBody DepositRequestDTO request)
    {
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
