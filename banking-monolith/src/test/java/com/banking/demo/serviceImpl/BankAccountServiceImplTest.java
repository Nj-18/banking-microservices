package com.banking.demo.serviceImpl;

import com.banking.demo.dtos.CreateBankAccountRequestDTO;
import com.banking.demo.dtos.DepositRequestDTO;
import com.banking.demo.dtos.TransferRequestDTO;
import com.banking.demo.dtos.WithdrawRequestDTO;
import com.banking.demo.entities.BankAccount;
import com.banking.demo.entities.Customer;
import com.banking.demo.entities.Transaction;
import com.banking.demo.exceptions.AccountNotFoundException;
import com.banking.demo.exceptions.CustomerNotFoundException;
import com.banking.demo.exceptions.InsufficientBalanceException;
import com.banking.demo.exceptions.InvalidAmountException;
import com.banking.demo.repositories.BankAccountRepository;
import com.banking.demo.repositories.CustomerRepository;
import com.banking.demo.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BankAccountServiceImpl bankAccountServiceImpl;


    // =========================================================
    // CREATE ACCOUNT
    // =========================================================

    @Test
    void createAccount_shouldCreateSuccessfully() {

        // ARRANGE
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Nikunj");
        customer.setLastName("Jain");

        CreateBankAccountRequestDTO request =
                new CreateBankAccountRequestDTO();

        request.setCustomerId(1L);
        request.setAccountType("SAVINGS");
        request.setOpeningBalance(5000.0);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        BankAccount result =
                bankAccountServiceImpl.createAccount(request);

        // ASSERT
        assertNotNull(result);
        assertEquals("SAVINGS", result.getAccountType());
        assertEquals(5000.0, result.getBalance());
        assertEquals("ACTIVE", result.getAccountStatus());
        assertNotNull(result.getAccountNumber());
        assertTrue(result.getAccountNumber().startsWith("ACC"));
        assertEquals(customer, result.getCustomer());

        // VERIFY
        verify(customerRepository, times(1))
                .findById(1L);

        verify(bankAccountRepository, times(1))
                .save(any(BankAccount.class));
    }


    @Test
    void createAccount_shouldThrowException_whenCustomerNotFound() {

        // ARRANGE
        CreateBankAccountRequestDTO request =
                new CreateBankAccountRequestDTO();

        request.setCustomerId(999L);
        request.setAccountType("SAVINGS");
        request.setOpeningBalance(5000.0);

        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        CustomerNotFoundException exception =
                assertThrows(
                        CustomerNotFoundException.class,
                        () -> bankAccountServiceImpl
                                .createAccount(request)
                );

        assertEquals(
                "Customer not found with id : 999",
                exception.getMessage()
        );

        // VERIFY
        verify(customerRepository, times(1))
                .findById(999L);

        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));
    }


    // =========================================================
    // DEPOSIT
    // =========================================================

    @Test
    void depositMoney_shouldDepositSuccessfully() {

        // ARRANGE
        BankAccount account = new BankAccount();

        account.setId(1L);
        account.setAccountNumber("ACC1001");
        account.setBalance(5000.0);
        account.setAccountStatus("ACTIVE");

        DepositRequestDTO request =
                new DepositRequestDTO();

        request.setAccountNumber("ACC1001");
        request.setAmount(2000.0);

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        var response =
                bankAccountServiceImpl.depositMoney(request);

        // ASSERT
        assertNotNull(response);

        assertEquals(
                "ACC1001",
                response.getAccountNumber()
        );

        assertEquals(
                2000.0,
                response.getDepositedAmount()
        );

        assertEquals(
                5000.0,
                response.getPreviousBalance()
        );

        assertEquals(
                7000.0,
                response.getUpdatedBalance()
        );

        assertEquals(
                7000.0,
                account.getBalance()
        );

        // VERIFY
        verify(bankAccountRepository, times(1))
                .findByAccountNumber("ACC1001");

        verify(bankAccountRepository, times(1))
                .save(account);

        verify(transactionRepository, times(1))
                .save(any(Transaction.class));
    }


    @Test
    void depositMoney_shouldThrowException_whenAmountIsZero() {

        // ARRANGE
        BankAccount account = new BankAccount();

        account.setAccountNumber("ACC1001");
        account.setBalance(5000.0);

        DepositRequestDTO request =
                new DepositRequestDTO();

        request.setAccountNumber("ACC1001");
        request.setAmount(0.0);

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        // ACT + ASSERT
        InvalidAmountException exception =
                assertThrows(
                        InvalidAmountException.class,
                        () -> bankAccountServiceImpl
                                .depositMoney(request)
                );

        assertEquals(
                "Deposit amount must be greater than zero.",
                exception.getMessage()
        );

        // VERIFY
        verify(bankAccountRepository, times(1))
                .findByAccountNumber("ACC1001");

        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    @Test
    void depositMoney_shouldThrowException_whenAmountIsNegative() {

        // ARRANGE
        BankAccount account = new BankAccount();

        account.setAccountNumber("ACC1001");
        account.setBalance(5000.0);

        DepositRequestDTO request =
                new DepositRequestDTO();

        request.setAccountNumber("ACC1001");
        request.setAmount(-1000.0);

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        // ACT + ASSERT
        assertThrows(
                InvalidAmountException.class,
                () -> bankAccountServiceImpl
                        .depositMoney(request)
        );

        // VERIFY
        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    @Test
    void depositMoney_shouldThrowException_whenAccountNotFound() {

        // ARRANGE
        DepositRequestDTO request =
                new DepositRequestDTO();

        request.setAccountNumber("INVALID");
        request.setAmount(1000.0);

        when(bankAccountRepository
                .findByAccountNumber("INVALID"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                AccountNotFoundException.class,
                () -> bankAccountServiceImpl
                        .depositMoney(request)
        );

        // VERIFY
        verify(bankAccountRepository, times(1))
                .findByAccountNumber("INVALID");

        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    // =========================================================
    // WITHDRAW
    // =========================================================

    @Test
    void withdrawMoney_shouldWithdrawSuccessfully() {

        // ARRANGE
        BankAccount account = new BankAccount();

        account.setId(1L);
        account.setAccountNumber("ACC1001");
        account.setBalance(10000.0);
        account.setAccountStatus("ACTIVE");

        WithdrawRequestDTO request =
                new WithdrawRequestDTO();

        request.setAccountNumber("ACC1001");
        request.setAmount(3000.0);

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        var response =
                bankAccountServiceImpl.withdrawMoney(request);

        // ASSERT
        assertNotNull(response);

        assertEquals(
                "ACC1001",
                response.getAccountNumber()
        );

        assertEquals(
                3000.0,
                response.getWithdrawnAmount()
        );

        assertEquals(
                10000.0,
                response.getPreviousBalance()
        );

        assertEquals(
                7000.0,
                response.getUpdatedBalance()
        );

        assertEquals(
                7000.0,
                account.getBalance()
        );

        // VERIFY
        verify(bankAccountRepository)
                .findByAccountNumber("ACC1001");

        verify(bankAccountRepository)
                .save(account);

        verify(transactionRepository)
                .save(any(Transaction.class));
    }


    @Test
    void withdrawMoney_shouldThrowException_whenInsufficientBalance() {

        // ARRANGE
        BankAccount account = new BankAccount();

        account.setAccountNumber("ACC1001");
        account.setBalance(2000.0);
        account.setAccountStatus("ACTIVE");

        WithdrawRequestDTO request =
                new WithdrawRequestDTO();

        request.setAccountNumber("ACC1001");
        request.setAmount(5000.0);

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        // ACT + ASSERT
        assertThrows(
                InsufficientBalanceException.class,
                () -> bankAccountServiceImpl
                        .withdrawMoney(request)
        );

        // VERIFY
        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    @Test
    void withdrawMoney_shouldThrowException_whenAccountInactive() {

        // ARRANGE
        BankAccount account = new BankAccount();

        account.setAccountNumber("ACC1001");
        account.setBalance(10000.0);
        account.setAccountStatus("BLOCKED");

        WithdrawRequestDTO request =
                new WithdrawRequestDTO();

        request.setAccountNumber("ACC1001");
        request.setAmount(1000.0);

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        // ACT + ASSERT
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> bankAccountServiceImpl
                                .withdrawMoney(request)
                );

        assertEquals(
                "Account is not active.",
                exception.getMessage()
        );

        // VERIFY
        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    // =========================================================
    // TRANSFER
    // =========================================================

    @Test
    void transferMoney_shouldTransferSuccessfully() {

        // ARRANGE

        BankAccount sender = new BankAccount();

        sender.setId(1L);
        sender.setAccountNumber("ACC1785153036227");
        sender.setBalance(7400.0);
        sender.setAccountStatus("ACTIVE");


        BankAccount receiver = new BankAccount();

        receiver.setId(2L);
        receiver.setAccountNumber("ACC1785153077734");
        receiver.setBalance(6100.0);
        receiver.setAccountStatus("ACTIVE");


        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber(
                "ACC1785153036227"
        );

        request.setToAccountNumber(
                "ACC1785153077734"
        );

        request.setAmount(2000.0);

        request.setRemarks("Test transfer");


        when(bankAccountRepository
                .findByAccountNumber("ACC1785153036227"))
                .thenReturn(Optional.of(sender));

        when(bankAccountRepository
                .findByAccountNumber("ACC1785153077734"))
                .thenReturn(Optional.of(receiver));

        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        // ACT

        var response =
                bankAccountServiceImpl
                        .transferMoney(request);


        // ASSERT

        assertNotNull(response);

        assertEquals(
                "ACC1785153036227",
                response.getFromAccount()
        );

        assertEquals(
                "ACC1785153077734",
                response.getToAccount()
        );

        assertEquals(
                2000.0,
                response.getAmount()
        );

        assertEquals(
                5400.0,
                response.getSenderBalance()
        );

        assertEquals(
                8100.0,
                response.getReceiverBalance()
        );

        assertEquals(
                5400.0,
                sender.getBalance()
        );

        assertEquals(
                8100.0,
                receiver.getBalance()
        );

        assertEquals(
                "Money transferred successfully.",
                response.getMessage()
        );


        // VERIFY

        verify(bankAccountRepository)
                .findByAccountNumber(
                        "ACC1785153036227"
                );

        verify(bankAccountRepository)
                .findByAccountNumber(
                        "ACC1785153077734"
                );

        verify(bankAccountRepository, times(2))
                .save(any(BankAccount.class));

        verify(transactionRepository, times(2))
                .save(any(Transaction.class));
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
                () -> bankAccountServiceImpl
                        .transferMoney(request)
        );

        // VERIFY
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
        request.setAmount(-100.0);

        // ACT + ASSERT
        assertThrows(
                InvalidAmountException.class,
                () -> bankAccountServiceImpl
                        .transferMoney(request)
        );

        // VERIFY
        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository);
    }


    @Test
    void transferMoney_shouldThrowException_whenAccountsAreSame() {

        // ARRANGE
        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1001");
        request.setAmount(1000.0);

        // ACT + ASSERT
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> bankAccountServiceImpl
                                .transferMoney(request)
                );

        assertEquals(
                "Source and destination accounts cannot be the same.",
                exception.getMessage()
        );

        // VERIFY
        verifyNoInteractions(bankAccountRepository);
    }


    @Test
    void transferMoney_shouldThrowException_whenSenderNotFound() {

        // ARRANGE
        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("INVALID");
        request.setToAccountNumber("ACC1002");
        request.setAmount(1000.0);

        when(bankAccountRepository
                .findByAccountNumber("INVALID"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                AccountNotFoundException.class,
                () -> bankAccountServiceImpl
                        .transferMoney(request)
        );

        // VERIFY
        verify(bankAccountRepository)
                .findByAccountNumber("INVALID");

        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));

        verifyNoInteractions(transactionRepository);
    }


    @Test
    void transferMoney_shouldThrowException_whenReceiverNotFound() {

        // ARRANGE
        BankAccount sender = new BankAccount();

        sender.setAccountNumber("ACC1001");
        sender.setBalance(5000.0);
        sender.setAccountStatus("ACTIVE");

        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("INVALID");
        request.setAmount(1000.0);

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(sender));

        when(bankAccountRepository
                .findByAccountNumber("INVALID"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(
                AccountNotFoundException.class,
                () -> bankAccountServiceImpl
                        .transferMoney(request)
        );

        // VERIFY
        verify(bankAccountRepository)
                .findByAccountNumber("ACC1001");

        verify(bankAccountRepository)
                .findByAccountNumber("INVALID");

        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));

        verifyNoInteractions(transactionRepository);
    }


    @Test
    void transferMoney_shouldThrowException_whenSenderInactive() {

        // ARRANGE
        BankAccount sender = new BankAccount();

        sender.setAccountNumber("ACC1001");
        sender.setBalance(5000.0);
        sender.setAccountStatus("BLOCKED");

        BankAccount receiver = new BankAccount();

        receiver.setAccountNumber("ACC1002");
        receiver.setBalance(5000.0);
        receiver.setAccountStatus("ACTIVE");

        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(1000.0);

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(sender));

        when(bankAccountRepository
                .findByAccountNumber("ACC1002"))
                .thenReturn(Optional.of(receiver));

        // ACT + ASSERT
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> bankAccountServiceImpl
                                .transferMoney(request)
                );

        assertEquals(
                "Sender account is not active.",
                exception.getMessage()
        );

        // VERIFY
        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));

        verifyNoInteractions(transactionRepository);
    }


    @Test
    void transferMoney_shouldThrowException_whenReceiverInactive() {

        // ARRANGE
        BankAccount sender = new BankAccount();

        sender.setAccountNumber("ACC1001");
        sender.setBalance(5000.0);
        sender.setAccountStatus("ACTIVE");

        BankAccount receiver = new BankAccount();

        receiver.setAccountNumber("ACC1002");
        receiver.setBalance(5000.0);
        receiver.setAccountStatus("BLOCKED");

        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(1000.0);

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(sender));

        when(bankAccountRepository
                .findByAccountNumber("ACC1002"))
                .thenReturn(Optional.of(receiver));

        // ACT + ASSERT
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> bankAccountServiceImpl
                                .transferMoney(request)
                );

        assertEquals(
                "Receiver account is not active.",
                exception.getMessage()
        );

        // VERIFY
        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));

        verifyNoInteractions(transactionRepository);
    }


    @Test
    void transferMoney_shouldThrowException_whenInsufficientBalance() {

        // ARRANGE
        BankAccount sender = new BankAccount();

        sender.setAccountNumber("ACC1001");
        sender.setBalance(1000.0);
        sender.setAccountStatus("ACTIVE");

        BankAccount receiver = new BankAccount();

        receiver.setAccountNumber("ACC1002");
        receiver.setBalance(5000.0);
        receiver.setAccountStatus("ACTIVE");

        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(5000.0);

        when(bankAccountRepository
                .findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(sender));

        when(bankAccountRepository
                .findByAccountNumber("ACC1002"))
                .thenReturn(Optional.of(receiver));

        // ACT + ASSERT
        InsufficientBalanceException exception =
                assertThrows(
                        InsufficientBalanceException.class,
                        () -> bankAccountServiceImpl
                                .transferMoney(request)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Insufficient balance")
        );

        // VERIFY
        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));

        verifyNoInteractions(transactionRepository);
    }


    @Test
    void transferMoney_shouldThrowException_whenAccountNumbersAreNull() {

        // ARRANGE
        TransferRequestDTO request =
                new TransferRequestDTO();

        request.setFromAccountNumber(null);
        request.setToAccountNumber(null);
        request.setAmount(1000.0);

        // ACT + ASSERT
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> bankAccountServiceImpl
                                .transferMoney(request)
                );

        assertEquals(
                "From account and To account are required.",
                exception.getMessage()
        );

        // VERIFY
        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository);
    }
}