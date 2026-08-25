package com.banking.transaction_service.repository;

import com.banking.transaction_service.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountNumber(String accountNumber);

    Page<Transaction> findByAccountNumberAndTransactionDateBetween(
            String accountNumber,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable);
}
