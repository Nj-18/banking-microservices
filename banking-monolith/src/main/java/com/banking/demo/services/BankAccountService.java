package com.banking.demo.services;

import com.banking.demo.dtos.*;
import com.banking.demo.entities.BankAccount;

public interface BankAccountService {
    BankAccount createAccount(CreateBankAccountRequestDTO request);
    DepositResponseDTO depositMoney(DepositRequestDTO request);
    WithdrawResponseDTO withdrawMoney(WithdrawRequestDTO request);
    TransferResponseDTO transferMoney(TransferRequestDTO request);
}
