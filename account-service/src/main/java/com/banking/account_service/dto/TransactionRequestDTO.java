package com.banking.account_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionRequestDTO {

    private String accountNumber;

    private String transactionReference;

    private String transactionType;

    private Double amount;

    private Double balanceAfterTransaction;

    private String remarks;
}
