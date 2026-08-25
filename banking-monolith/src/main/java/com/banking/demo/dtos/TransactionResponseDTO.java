package com.banking.demo.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class TransactionResponseDTO {
    private String transactionReference;

    private String transactionType;

    private Double amount;

    private Double balanceAfterTransaction;

    private String status;

    private LocalDateTime transactionDate;
}
