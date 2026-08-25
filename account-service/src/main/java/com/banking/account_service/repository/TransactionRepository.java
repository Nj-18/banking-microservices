package com.banking.account_service.repository;

import com.banking.account_service.entity.BankAccount;
import com.banking.account_service.entity.Transaction;
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