package com.banking.account_service.mappers;

import com.banking.account_service.dto.TransactionResponseDTO;
import com.banking.account_service.entity.Transaction;

public class TransactionMapper {

    public static TransactionResponseDTO toDTO(Transaction transaction){

        TransactionResponseDTO dto = new TransactionResponseDTO();

        dto.setTransactionReference(transaction.getTransactionReference());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setAmount(transaction.getAmount());
        dto.setBalanceAfterTransaction(transaction.getBalanceAfterTransaction());
        dto.setStatus(transaction.getStatus());
        dto.setTransactionDate(transaction.getTransactionDate());

        return dto;
    }
}
