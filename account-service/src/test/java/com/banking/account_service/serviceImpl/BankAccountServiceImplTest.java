package com.banking.account_service.serviceImpl;

import com.banking.account_service.client.CustomerClient;
import com.banking.account_service.client.TransactionClient;
import com.banking.account_service.dto.*;
import com.banking.account_service.entity.BankAccount;
import com.banking.account_service.exception.AccountNotFoundException;
import com.banking.account_service.exception.CustomerNotFoundException;
import com.banking.account_service.exception.InsufficientBalanceException;
import com.banking.account_service.exception.InvalidAmountException;
import com.banking.account_service.repository.BankAccountRepository;
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
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private TransactionClient transactionClient;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    @Test
    void createAccount_shouldSaveWhenCustomerExists() {
        CreateBankAccountRequestDTO request = new CreateBankAccountRequestDTO();
        request.setCustomerId(10L);
        request.setAccountType("SAVINGS");
        request.setOpeningBalance(1000.0);

        CustomerDTO customer = new CustomerDTO();
        customer.setId(10L);
        when(customerClient.getCustomer(10L)).thenReturn(customer);
        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount saved = bankAccountService.createAccount(request);

        assertEquals("SAVINGS", saved.getAccountType());
        assertEquals(1000.0, saved.getBalance());
        assertEquals("ACTIVE", saved.getAccountStatus());
        assertEquals(10L, saved.getCustomerId());
        assertTrue(saved.getAccountNumber().startsWith("ACC"));
        verify(transactionClient, never()).recordTransaction(any());
    }

    @Test
    void createAccount_shouldThrowWhenCustomerMissing() {
        CreateBankAccountRequestDTO request = new CreateBankAccountRequestDTO();
        request.setCustomerId(99L);
        when(customerClient.getCustomer(99L)).thenReturn(null);

        assertThrows(CustomerNotFoundException.class,
                () -> bankAccountService.createAccount(request));
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void getAccountByAccountNumber_shouldReturnAccount() {
        BankAccount account = account("ACC1001", 5000.0, "ACTIVE");
        when(bankAccountRepository.findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        BankAccount result = bankAccountService.getAccountByAccountNumber("ACC1001");

        assertEquals("ACC1001", result.getAccountNumber());
    }

    @Test
    void getAccountByAccountNumber_shouldThrowWhenMissing() {
        when(bankAccountRepository.findByAccountNumber("MISSING"))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> bankAccountService.getAccountByAccountNumber("MISSING"));
    }

    @Test
    void depositMoney_shouldUpdateBalanceAndRecordTransaction() {
        BankAccount account = account("ACC1001", 5000.0, "ACTIVE");
        when(bankAccountRepository.findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));
        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DepositRequestDTO request = new DepositRequestDTO("ACC1001", 1500.0);
        DepositResponseDTO response = bankAccountService.depositMoney(request);

        assertEquals(5000.0, response.getPreviousBalance());
        assertEquals(6500.0, response.getUpdatedBalance());
        assertEquals(1500.0, response.getDepositedAmount());

        ArgumentCaptor<TransactionRequestDTO> captor =
                ArgumentCaptor.forClass(TransactionRequestDTO.class);
        verify(transactionClient).recordTransaction(captor.capture());
        assertEquals("DEPOSIT", captor.getValue().getTransactionType());
        assertEquals(6500.0, captor.getValue().getBalanceAfterTransaction());
        assertEquals("ACC1001", captor.getValue().getAccountNumber());
    }

    @Test
    void depositMoney_shouldRejectNonPositiveAmount() {
        BankAccount account = account("ACC1001", 5000.0, "ACTIVE");
        when(bankAccountRepository.findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        assertThrows(InvalidAmountException.class,
                () -> bankAccountService.depositMoney(new DepositRequestDTO("ACC1001", 0.0)));
        verify(transactionClient, never()).recordTransaction(any());
    }

    @Test
    void withdrawMoney_shouldUpdateBalanceAndRecordTransaction() {
        BankAccount account = account("ACC1001", 5000.0, "ACTIVE");
        when(bankAccountRepository.findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));
        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawResponseDTO response = bankAccountService.withdrawMoney(
                new WithdrawRequestDTO("ACC1001", 1200.0));

        assertEquals(5000.0, response.getPreviousBalance());
        assertEquals(3800.0, response.getUpdatedBalance());

        ArgumentCaptor<TransactionRequestDTO> captor =
                ArgumentCaptor.forClass(TransactionRequestDTO.class);
        verify(transactionClient).recordTransaction(captor.capture());
        assertEquals("WITHDRAW", captor.getValue().getTransactionType());
        assertEquals(3800.0, captor.getValue().getBalanceAfterTransaction());
    }

    @Test
    void withdrawMoney_shouldRejectInsufficientBalance() {
        BankAccount account = account("ACC1001", 500.0, "ACTIVE");
        when(bankAccountRepository.findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        assertThrows(InsufficientBalanceException.class,
                () -> bankAccountService.withdrawMoney(
                        new WithdrawRequestDTO("ACC1001", 600.0)));
        verify(transactionClient, never()).recordTransaction(any());
    }

    @Test
    void transferMoney_shouldMoveFundsAndRecordDebitAndCredit() {
        BankAccount sender = account("ACC1001", 5000.0, "ACTIVE");
        BankAccount receiver = account("ACC1002", 2000.0, "ACTIVE");
        when(bankAccountRepository.findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(sender));
        when(bankAccountRepository.findByAccountNumber("ACC1002"))
                .thenReturn(Optional.of(receiver));
        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(1000.0);
        request.setRemarks("Rent");

        TransferResponseDTO response = bankAccountService.transferMoney(request);

        assertEquals(4000.0, response.getSenderBalance());
        assertEquals(3000.0, response.getReceiverBalance());
        assertEquals("Money transferred successfully.", response.getMessage());
        assertNotNull(response.getTransactionReference());

        ArgumentCaptor<TransactionRequestDTO> captor =
                ArgumentCaptor.forClass(TransactionRequestDTO.class);
        verify(transactionClient, times(2)).recordTransaction(captor.capture());

        TransactionRequestDTO debit = captor.getAllValues().get(0);
        TransactionRequestDTO credit = captor.getAllValues().get(1);
        assertEquals("TRANSFER_DEBIT", debit.getTransactionType());
        assertEquals("ACC1001", debit.getAccountNumber());
        assertEquals("TRANSFER_CREDIT", credit.getTransactionType());
        assertEquals("ACC1002", credit.getAccountNumber());
        assertEquals(debit.getTransactionReference(), credit.getTransactionReference());
    }

    @Test
    void transferMoney_shouldRejectSameAccount() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1001");
        request.setAmount(100.0);

        assertThrows(RuntimeException.class, () -> bankAccountService.transferMoney(request));
        verifyNoInteractions(transactionClient);
    }

    @Test
    void transferMoney_shouldRejectInvalidAmount() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(-10.0);

        assertThrows(InvalidAmountException.class,
                () -> bankAccountService.transferMoney(request));
        verifyNoInteractions(transactionClient);
    }

    private BankAccount account(String number, Double balance, String status) {
        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setAccountNumber(number);
        account.setBalance(balance);
        account.setAccountStatus(status);
        account.setCustomerId(10L);
        return account;
    }
}
