package com.banking.transaction_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionReference;

    private String transactionType;

    private Double amount;

    private Double balanceAfterTransaction;

    private String status;

    private String remarks;

    private LocalDateTime transactionDate;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;
}
