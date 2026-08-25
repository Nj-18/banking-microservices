package com.banking.account_service.service;

import com.banking.account_service.dto.*;
import com.banking.account_service.entity.BankAccount;

public interface BankAccountService {
    BankAccount createAccount(CreateBankAccountRequestDTO request);
    DepositResponseDTO depositMoney(DepositRequestDTO request);
    WithdrawResponseDTO withdrawMoney(WithdrawRequestDTO request);
    TransferResponseDTO transferMoney(TransferRequestDTO request);
}
