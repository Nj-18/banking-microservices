package com.banking.demo.repositories;

import com.banking.demo.entities.BankAccount;
import com.banking.demo.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBankAccount(BankAccount account);
    Page<Transaction> findByBankAccountAndTransactionDateBetween(
            BankAccount bankAccount,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable);
}