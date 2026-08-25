package com.banking.demo.services;

import com.banking.demo.dtos.StatementResponseDTO;
import com.banking.demo.dtos.TransactionResponseDTO;
import com.banking.demo.dtos.TransferRequestDTO;
import com.banking.demo.dtos.TransferResponseDTO;
import com.banking.demo.entities.BankAccount;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    List<TransactionResponseDTO> getTransactions(String accountNumber);

    void saveTransaction(
            BankAccount account,
            String transactionReference,
            String transactionType,
            Double amount,
            String remarks);

    @Transactional
    TransferResponseDTO transferMoney(TransferRequestDTO request);

    StatementResponseDTO getStatement(String accountNumber, LocalDate fromDate, LocalDate toDate, int page, int size);
}