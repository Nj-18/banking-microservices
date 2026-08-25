package com.banking.demo.dtos;

import com.banking.demo.entities.Transaction;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatementResponseDTO {

    private String accountNumber;

    private Double currentBalance;

    private List<TransactionResponseDTO> transactions;

//    public void setTransactions(List<Transaction> transactions) {
//    }
}