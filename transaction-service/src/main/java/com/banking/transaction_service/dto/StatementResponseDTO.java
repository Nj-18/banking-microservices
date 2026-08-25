package com.banking.transaction_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatementResponseDTO {

    private String accountNumber;

    private Double currentBalance;

    private List<TransactionResponseDTO> transactions;
}
