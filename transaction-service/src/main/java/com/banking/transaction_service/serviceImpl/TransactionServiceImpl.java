package com.banking.transaction_service.serviceImpl;

import com.banking.transaction_service.client.AccountClient;
import com.banking.transaction_service.dto.AccountDTO;
import com.banking.transaction_service.dto.StatementResponseDTO;
import com.banking.transaction_service.dto.TransactionRequestDTO;
import com.banking.transaction_service.dto.TransactionResponseDTO;
import com.banking.transaction_service.dto.TransferRequestDTO;
import com.banking.transaction_service.dto.TransferResponseDTO;
import com.banking.transaction_service.entity.Transaction;
import com.banking.transaction_service.exception.AccountNotFoundException;
import com.banking.transaction_service.mapper.TransactionMapper;
import com.banking.transaction_service.repository.TransactionRepository;
import com.banking.transaction_service.service.TransactionService;
import feign.FeignException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.banking.transaction_service.event.TransactionEvent;
import com.banking.transaction_service.kafka.KafkaProducerService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final KafkaProducerService kafkaProducerService;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            AccountClient accountClient,
            KafkaProducerService kafkaProducerService
            ) {

        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
        this.kafkaProducerService=kafkaProducerService;
    }

    @Override
    public List<TransactionResponseDTO> getTransactions(String accountNumber) {
        AccountDTO account = getAccount(accountNumber);

        List<Transaction> transactions =
                transactionRepository.findByAccountNumber(account.getAccountNumber());

        return transactions.stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Transaction saveTransaction(TransactionRequestDTO request) {
        Transaction transaction = new Transaction();

        transaction.setAccountNumber(request.getAccountNumber());
        transaction.setTransactionReference(request.getTransactionReference());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(request.getAmount());
        transaction.setBalanceAfterTransaction(request.getBalanceAfterTransaction());
        transaction.setRemarks(request.getRemarks());
        transaction.setStatus("SUCCESS");
        transaction.setTransactionDate(LocalDateTime.now());

        Transaction saved = transactionRepository.save(transaction);

        TransactionEvent event = new TransactionEvent();
        event.setTransactionReference(saved.getTransactionReference());
        event.setFromAccountNumber(saved.getAccountNumber());
        event.setAmount(java.math.BigDecimal.valueOf(saved.getAmount()));
        event.setTransactionType(saved.getTransactionType());
        event.setStatus(saved.getStatus());
        event.setTimestamp(saved.getTransactionDate());

        kafkaProducerService.sendTransactionEvent(event);

        return saved;
    }

    @Override
    public TransferResponseDTO transferMoney(TransferRequestDTO request) {

        System.out.println("========== TRANSFER STARTED ==========");

        TransferResponseDTO response = accountClient.transferMoney(request);

        System.out.println("Account servicex transfer completed");
        System.out.println("Transaction Reference: " + response.getTransactionReference());

        TransactionEvent event = new TransactionEvent();

        event.setTransactionReference(response.getTransactionReference());
        event.setFromAccountNumber(request.getFromAccountNumber());
        event.setToAccountNumber(request.getToAccountNumber());
        event.setAmount(
                java.math.BigDecimal.valueOf(request.getAmount())
        );
        event.setTransactionType("TRANSFER");
        event.setStatus("SUCCESS");
        event.setTimestamp(LocalDateTime.now());

        System.out.println("Sending TransactionEvent to Kafka...");

        kafkaProducerService.sendTransactionEvent(event);

        System.out.println("========== TRANSFER COMPLETED ==========");

        return response;
    }

    @Override
    public StatementResponseDTO getStatement(
            String accountNumber,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {

        AccountDTO account = getAccount(accountNumber);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("transactionDate").descending());

        Page<Transaction> transactions =
                transactionRepository.findByAccountNumberAndTransactionDateBetween(
                        account.getAccountNumber(),
                        fromDate.atStartOfDay(),
                        toDate.atTime(LocalTime.MAX),
                        pageable);

        List<TransactionResponseDTO> transactionDTOs =
                transactions.getContent()
                        .stream()
                        .map(TransactionMapper::toDTO)
                        .collect(Collectors.toList());

        StatementResponseDTO responseDTO = new StatementResponseDTO();
        responseDTO.setAccountNumber(account.getAccountNumber());
        responseDTO.setCurrentBalance(account.getBalance());
        responseDTO.setTransactions(transactionDTOs);

        return responseDTO;
    }

    private AccountDTO getAccount(String accountNumber) {
        try {
            AccountDTO account = accountClient.getAccount(accountNumber);
            if (account == null) {
                throw new AccountNotFoundException(
                        "Account not found with account number : " + accountNumber);
            }
            return account;
        } catch (FeignException.NotFound ex) {
            throw new AccountNotFoundException(
                    "Account not found with account number : " + accountNumber);
        }
    }
}
