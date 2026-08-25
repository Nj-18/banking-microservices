package com.banking.demo.repositories;

import com.banking.demo.dtos.DepositRequestDTO;
import com.banking.demo.dtos.DepositResponseDTO;
import com.banking.demo.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    //DepositResponseDTO depositMoney(DepositRequestDTO request);
}