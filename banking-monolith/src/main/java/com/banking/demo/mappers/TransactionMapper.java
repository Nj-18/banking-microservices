package com.banking.demo.mappers;

import com.banking.demo.dtos.TransactionResponseDTO;
import com.banking.demo.entities.Transaction;

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
