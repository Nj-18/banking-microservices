package com.banking.demo.serviceImpl;

import com.banking.demo.dtos.*;
import com.banking.demo.entities.BankAccount;
import com.banking.demo.entities.Transaction;
import com.banking.demo.exceptions.*;
import com.banking.demo.mappers.TransactionMapper;
import com.banking.demo.repositories.BankAccountRepository;
import com.banking.demo.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    // =========================================================
    // getTransactions()
    // =========================================================

    @Test
    void getTransactions_shouldReturnTransactionsSuccessfully() {

        // ARRANGE

        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setAccountNumber("ACC1001");
        account.setBalance(5000.0);
        account.setAccountStatus("ACTIVE");

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionReference("TXN123");
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(1000.0);
        transaction.setStatus("SUCCESS");
        transaction.setBankAccount(account);

        when(bankAccountRepository.findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        when(transactionRepository.findByBankAccount(account))
                .thenReturn(List.of(transaction));

        TransactionResponseDTO dto = new TransactionResponseDTO();

        try (MockedStatic<TransactionMapper> mockedMapper =
                     mockStatic(TransactionMapper.class)) {

            mockedMapper.when(() -> TransactionMapper.toDTO(transaction))
                    .thenReturn(dto);

            // ACT

            List<TransactionResponseDTO> result =
                    transactionService.getTransactions("ACC1001");

            // ASSERT

            assertNotNull(result);
            assertEquals(1, result.size());
            assertSame(dto, result.get(0));

            verify(bankAccountRepository)
                    .findByAccountNumber("ACC1001");

            verify(transactionRepository)
                    .findByBankAccount(account);
        }
    }


    @Test
    void getTransactions_shouldReturnEmptyList_whenNoTransactions() {

        // ARRANGE

        BankAccount account = new BankAccount();
        account.setAccountNumber("ACC1001");

        when(bankAccountRepository.findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        when(transactionRepository.findByBankAccount(account))
                .thenReturn(List.of());

        // ACT

        List<TransactionResponseDTO> result =
                transactionService.getTransactions("ACC1001");

        // ASSERT

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(transactionRepository)
                .findByBankAccount(account);
    }


    @Test
    void getTransactions_shouldThrowException_whenAccountNotFound() {

        // ARRANGE

        when(bankAccountRepository.findByAccountNumber("INVALID"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT

        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.getTransactions("INVALID")
        );

        verify(bankAccountRepository)
                .findByAccountNumber("INVALID");

        verifyNoInteractions(transactionRepository);
    }


    // =========================================================
    // saveTransaction()
    // =========================================================

    @Test
    void saveTransaction_shouldSaveTransactionSuccessfully() {

        // ARRANGE

        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setAccountNumber("ACC1001");

        // ACT

        transactionService.saveTransaction(
                account,
                "TXN123",
                "TRANSFER_DEBIT",
                500.0,
                "Money transfer"
        );

        // ASSERT

        ArgumentCaptor<Transaction> captor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository)
                .save(captor.capture());

        Transaction savedTransaction = captor.getValue();

        assertEquals("TXN123",
                savedTransaction.getTransactionReference());

        assertEquals("TRANSFER_DEBIT",
                savedTransaction.getTransactionType());

        assertEquals(500.0,
                savedTransaction.getAmount());

        assertEquals("Money transfer",
                savedTransaction.getRemarks());

        assertEquals("SUCCESS",
                savedTransaction.getStatus());

        assertNotNull(
                savedTransaction.getTransactionDate());

        assertSame(
                account,
                savedTransaction.getBankAccount());
    }


    // =========================================================
    // transferMoney()
    // =========================================================

    @Test
    void transferMoney_shouldTransferSuccessfully() {

        // ARRANGE

        BankAccount sender = new BankAccount();
        sender.setId(1L);
        sender.setAccountNumber("ACC1001");
        sender.setBalance(5000.0);
        sender.setAccountStatus("ACTIVE");

        BankAccount receiver = new BankAccount();
        receiver.setId(2L);
        receiver.setAccountNumber("ACC1002");
        receiver.setBalance(2000.0);
        receiver.setAccountStatus("ACTIVE");

        TransferRequestDTO request = new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(1000.0);
        request.setRemarks("Test transfer");

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(sender));

        when(bankAccountRepository
                .findByAccountNumber("ACC1002"))
                .thenReturn(Optional.of(receiver));

        // ACT

        TransferResponseDTO response =
                transactionService.transferMoney(request);

        // ASSERT

        assertNotNull(response);

        assertEquals(
                "ACC1001",
                response.getFromAccount());

        assertEquals(
                "ACC1002",
                response.getToAccount());

        assertEquals(
                1000.0,
                response.getAmount());

        assertEquals(
                4000.0,
                response.getSenderBalance());

        assertEquals(
                3000.0,
                response.getReceiverBalance());

        assertEquals(
                "Money transferred successfully.",
                response.getMessage());

        assertNotNull(
                response.getTransactionReference());

        // Verify account saves

        verify(bankAccountRepository)
                .save(sender);

        verify(bankAccountRepository)
                .save(receiver);

        // Two transactions should be saved

        verify(transactionRepository, times(2))
                .save(any(Transaction.class));
    }


    @Test
    void transferMoney_shouldThrowException_whenSameAccount() {

        // ARRANGE

        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1001");
        request.setAmount(1000.0);

        // ACT + ASSERT

        assertThrows(
                SameAccountTransferException.class,
                () -> transactionService.transferMoney(request)
        );

        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository);
    }


    @Test
    void transferMoney_shouldThrowException_whenAmountIsZero() {

        // ARRANGE

        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(0.0);

        // ACT + ASSERT

        assertThrows(
                InvalidAmountException.class,
                () -> transactionService.transferMoney(request)
        );

        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository);
    }


    @Test
    void transferMoney_shouldThrowException_whenAmountIsNegative() {

        // ARRANGE

        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(-500.0);

        // ACT + ASSERT

        assertThrows(
                InvalidAmountException.class,
                () -> transactionService.transferMoney(request)
        );

        verifyNoInteractions(bankAccountRepository);
    }


    @Test
    void transferMoney_shouldThrowException_whenSenderNotFound() {

        // ARRANGE

        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(500.0);

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT

        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.transferMoney(request)
        );

        verify(bankAccountRepository)
                .findByAccountNumber("ACC1001");

        verify(bankAccountRepository, never())
                .save(any());
    }


    @Test
    void transferMoney_shouldThrowException_whenReceiverNotFound() {

        // ARRANGE

        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(500.0);

        BankAccount sender = new BankAccount();

        sender.setAccountNumber("ACC1001");
        sender.setBalance(5000.0);
        sender.setAccountStatus("ACTIVE");

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(sender));

        when(bankAccountRepository
                .findByAccountNumber("ACC1002"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT

        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.transferMoney(request)
        );

        verify(bankAccountRepository, never())
                .save(any());
    }


    @Test
    void transferMoney_shouldThrowException_whenSenderInactive() {

        // ARRANGE

        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(500.0);

        BankAccount sender = new BankAccount();

        sender.setAccountNumber("ACC1001");
        sender.setBalance(5000.0);
        sender.setAccountStatus("INACTIVE");

        BankAccount receiver = new BankAccount();

        receiver.setAccountNumber("ACC1002");
        receiver.setBalance(2000.0);
        receiver.setAccountStatus("ACTIVE");

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(sender));

        when(bankAccountRepository
                .findByAccountNumber("ACC1002"))
                .thenReturn(Optional.of(receiver));

        // ACT + ASSERT

        assertThrows(
                AccountInactiveException.class,
                () -> transactionService.transferMoney(request)
        );

        verify(bankAccountRepository, never())
                .save(any());

        verifyNoInteractions(transactionRepository);
    }


    @Test
    void transferMoney_shouldThrowException_whenReceiverInactive() {

        // ARRANGE

        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(500.0);

        BankAccount sender = new BankAccount();

        sender.setAccountNumber("ACC1001");
        sender.setBalance(5000.0);
        sender.setAccountStatus("ACTIVE");

        BankAccount receiver = new BankAccount();

        receiver.setAccountNumber("ACC1002");
        receiver.setBalance(2000.0);
        receiver.setAccountStatus("INACTIVE");

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(sender));

        when(bankAccountRepository
                .findByAccountNumber("ACC1002"))
                .thenReturn(Optional.of(receiver));

        // ACT + ASSERT

        assertThrows(
                AccountInactiveException.class,
                () -> transactionService.transferMoney(request)
        );

        verify(bankAccountRepository, never())
                .save(any());

        verifyNoInteractions(transactionRepository);
    }


    @Test
    void transferMoney_shouldThrowException_whenInsufficientBalance() {

        // ARRANGE

        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(6000.0);

        BankAccount sender = new BankAccount();

        sender.setAccountNumber("ACC1001");
        sender.setBalance(5000.0);
        sender.setAccountStatus("ACTIVE");

        BankAccount receiver = new BankAccount();

        receiver.setAccountNumber("ACC1002");
        receiver.setBalance(2000.0);
        receiver.setAccountStatus("ACTIVE");

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(sender));

        when(bankAccountRepository
                .findByAccountNumber("ACC1002"))
                .thenReturn(Optional.of(receiver));

        // ACT + ASSERT

        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.transferMoney(request)
        );

        verify(bankAccountRepository, never())
                .save(any());

        verifyNoInteractions(transactionRepository);
    }


    // =========================================================
    // getStatement()
    // =========================================================

    @Test
    void getStatement_shouldReturnStatementSuccessfully() {

        // ARRANGE

        BankAccount account = new BankAccount();

        account.setId(1L);
        account.setAccountNumber("ACC1001");
        account.setBalance(5000.0);

        LocalDate fromDate =
                LocalDate.of(2026, 1, 1);

        LocalDate toDate =
                LocalDate.of(2026, 1, 31);

        Pageable pageable =
                PageRequest.of(
                        0,
                        10,
                        Sort.by("transactionDate").descending());

        Page<Transaction> page =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0);

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        when(transactionRepository
                .findByBankAccountAndTransactionDateBetween(
                        eq(account),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        any(Pageable.class)))
                .thenReturn(page);

        // ACT

        StatementResponseDTO response =
                transactionService.getStatement(
                        "ACC1001",
                        fromDate,
                        toDate,
                        0,
                        10);

        // ASSERT

        assertNotNull(response);

        assertEquals(
                "ACC1001",
                response.getAccountNumber());

        assertEquals(
                5000.0,
                response.getCurrentBalance());

        assertNotNull(response.getTransactions());

        assertTrue(response.getTransactions().isEmpty());

        verify(bankAccountRepository)
                .findByAccountNumber("ACC1001");

        verify(transactionRepository)
                .findByBankAccountAndTransactionDateBetween(
                        eq(account),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        any(Pageable.class));
    }


    @Test
    void getStatement_shouldThrowException_whenAccountNotFound() {

        // ARRANGE

        when(bankAccountRepository
                .findByAccountNumber("INVALID"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT

        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.getStatement(
                        "INVALID",
                        LocalDate.now().minusDays(10),
                        LocalDate.now(),
                        0,
                        10)
        );

        verifyNoInteractions(transactionRepository);
    }
}