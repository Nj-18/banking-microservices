package com.banking.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "account_id")
    private BankAccount bankAccount;
}
