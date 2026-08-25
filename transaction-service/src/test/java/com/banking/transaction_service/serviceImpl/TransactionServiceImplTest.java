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
import com.banking.transaction_service.repository.TransactionRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void getTransactions_shouldReturnTransactionsSuccessfully() {
        AccountDTO account = account("ACC1001", 5000.0);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionReference("TXN123");
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(1000.0);
        transaction.setStatus("SUCCESS");
        transaction.setAccountNumber("ACC1001");

        when(accountClient.getAccount("ACC1001")).thenReturn(account);
        when(transactionRepository.findByAccountNumber("ACC1001"))
                .thenReturn(List.of(transaction));

        List<TransactionResponseDTO> result =
                transactionService.getTransactions("ACC1001");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TXN123", result.get(0).getTransactionReference());
        verify(accountClient).getAccount("ACC1001");
        verify(transactionRepository).findByAccountNumber("ACC1001");
    }

    @Test
    void getTransactions_shouldReturnEmptyList_whenNoTransactions() {
        AccountDTO account = account("ACC1001", 5000.0);

        when(accountClient.getAccount("ACC1001")).thenReturn(account);
        when(transactionRepository.findByAccountNumber("ACC1001"))
                .thenReturn(List.of());

        List<TransactionResponseDTO> result =
                transactionService.getTransactions("ACC1001");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTransactions_shouldThrowException_whenAccountNotFound() {
        when(accountClient.getAccount("INVALID"))
                .thenThrow(notFound());

        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.getTransactions("INVALID"));

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void saveTransaction_shouldSaveTransactionSuccessfully() {
        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAccountNumber("ACC1001");
        request.setTransactionReference("TXN123");
        request.setTransactionType("TRANSFER_DEBIT");
        request.setAmount(500.0);
        request.setBalanceAfterTransaction(4500.0);
        request.setRemarks("Money transfer");

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction saved = transactionService.saveTransaction(request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction captured = captor.getValue();
        assertEquals("TXN123", captured.getTransactionReference());
        assertEquals("TRANSFER_DEBIT", captured.getTransactionType());
        assertEquals(500.0, captured.getAmount());
        assertEquals("Money transfer", captured.getRemarks());
        assertEquals("SUCCESS", captured.getStatus());
        assertEquals("ACC1001", captured.getAccountNumber());
        assertNotNull(captured.getTransactionDate());
        assertSame(captured, saved);
    }

    @Test
    void transferMoney_shouldDelegateToAccountService() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(1000.0);

        TransferResponseDTO response = new TransferResponseDTO();
        response.setFromAccount("ACC1001");
        response.setToAccount("ACC1002");
        response.setAmount(1000.0);
        response.setMessage("Money transferred successfully.");

        when(accountClient.transferMoney(request)).thenReturn(response);

        TransferResponseDTO result = transactionService.transferMoney(request);

        assertEquals("ACC1001", result.getFromAccount());
        assertEquals("ACC1002", result.getToAccount());
        verify(accountClient).transferMoney(request);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getStatement_shouldReturnStatementSuccessfully() {
        AccountDTO account = account("ACC1001", 5000.0);
        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 1, 31);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("transactionDate").descending());

        when(accountClient.getAccount("ACC1001")).thenReturn(account);
        when(transactionRepository.findByAccountNumberAndTransactionDateBetween(
                eq("ACC1001"),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        StatementResponseDTO response = transactionService.getStatement(
                "ACC1001", fromDate, toDate, 0, 10);

        assertEquals("ACC1001", response.getAccountNumber());
        assertEquals(5000.0, response.getCurrentBalance());
        assertTrue(response.getTransactions().isEmpty());
    }

    @Test
    void getStatement_shouldThrowException_whenAccountNotFound() {
        when(accountClient.getAccount("INVALID")).thenThrow(notFound());

        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.getStatement(
                        "INVALID",
                        LocalDate.now().minusDays(10),
                        LocalDate.now(),
                        0,
                        10));

        verifyNoInteractions(transactionRepository);
    }

    private AccountDTO account(String accountNumber, Double balance) {
        AccountDTO account = new AccountDTO();
        account.setId(1L);
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setAccountStatus("ACTIVE");
        return account;
    }

    private FeignException.NotFound notFound() {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/account/INVALID",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate());
        return new FeignException.NotFound("not found", request, null, Collections.emptyMap());
    }
}
