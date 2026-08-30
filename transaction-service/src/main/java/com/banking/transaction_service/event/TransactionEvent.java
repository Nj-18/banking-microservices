package com.banking.transaction_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {

    private String transactionReference;

    private String fromAccountNumber;

    private String toAccountNumber;

    private BigDecimal amount;

    private String transactionType;

    private String status;

    private LocalDateTime timestamp;
}