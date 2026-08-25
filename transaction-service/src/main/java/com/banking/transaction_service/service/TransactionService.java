package com.banking.transaction_service.service;

import com.banking.transaction_service.dto.StatementResponseDTO;
import com.banking.transaction_service.dto.TransactionRequestDTO;
import com.banking.transaction_service.dto.TransactionResponseDTO;
import com.banking.transaction_service.dto.TransferRequestDTO;
import com.banking.transaction_service.dto.TransferResponseDTO;
import com.banking.transaction_service.entity.Transaction;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    List<TransactionResponseDTO> getTransactions(String accountNumber);

    Transaction saveTransaction(TransactionRequestDTO request);

    TransferResponseDTO transferMoney(TransferRequestDTO request);

    StatementResponseDTO getStatement(
            String accountNumber,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size);
}
