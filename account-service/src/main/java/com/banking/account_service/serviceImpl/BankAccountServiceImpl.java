package com.banking.account_service.serviceImpl;

import com.banking.account_service.client.CustomerClient;
import com.banking.account_service.dto.*;
import com.banking.account_service.entity.BankAccount;
//import com.banking.account_service.entity.Customer;
import com.banking.account_service.entity.Transaction;
import com.banking.account_service.exception.AccountNotFoundException;
import com.banking.account_service.exception.CustomerNotFoundException;
import com.banking.account_service.exception.InsufficientBalanceException;
import com.banking.account_service.exception.InvalidAmountException;
import com.banking.account_service.repository.BankAccountRepository;
//import com.banking.account_service.repository.CustomerRepository;
import com.banking.account_service.repository.TransactionRepository;
import com.banking.account_service.service.BankAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    //private final CustomerRepository customerRepository;
    private final CustomerClient customerClient;

    public BankAccountServiceImpl(
            TransactionRepository transactionRepository,
            BankAccountRepository bankAccountRepository,
            CustomerClient customerClient) {

        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.customerClient = customerClient;
    }

    @Override
    public BankAccount createAccount(CreateBankAccountRequestDTO request) {
        BankAccount bankAccount = new BankAccount();

        bankAccount.setAccountType(request.getAccountType());
        bankAccount.setBalance(request.getOpeningBalance());
        bankAccount.setAccountNumber("ACC" + System.currentTimeMillis());
        bankAccount.setAccountStatus("ACTIVE");

        CustomerDTO customer = customerClient.getCustomer(request.getCustomerId());

        if (customer == null) {
            throw new CustomerNotFoundException(
                    "Customer not found with id: " + request.getCustomerId());
        }

        bankAccount.setCustomerId(request.getCustomerId());
        //bankAccount.setCustomer(customer);

        return bankAccountRepository.save(bankAccount);

        //bankAccount.setCustomer();
        //Customer customer = customerRepository.findById(request.getCustomerId())
                // .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @Transactional
    @Override
    public TransferResponseDTO transferMoney(TransferRequestDTO request) {

        // 1. Validate amount
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new InvalidAmountException(
                    "Transfer amount must be greater than zero."
            );
        }

        // 2. Validate account numbers
        if (request.getFromAccountNumber() == null
                || request.getToAccountNumber() == null) {

            throw new RuntimeException(
                    "From account and To account are required."
            );
        }

        // 3. Prevent transfer to same account
        if (request.getFromAccountNumber()
                .equals(request.getToAccountNumber())) {

            throw new RuntimeException(
                    "Source and destination accounts cannot be the same."
            );
        }

        // 4. Find sender
        BankAccount sender = bankAccountRepository
                .findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Sender account not found: "
                                        + request.getFromAccountNumber()
                        )
                );

        // 5. Find receiver
        BankAccount receiver = bankAccountRepository
                .findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Receiver account not found: "
                                        + request.getToAccountNumber()
                        )
                );

        // 6. Check sender status
        if (!"ACTIVE".equalsIgnoreCase(sender.getAccountStatus())) {
            throw new RuntimeException(
                    "Sender account is not active."
            );
        }

        // 7. Check receiver status
        if (!"ACTIVE".equalsIgnoreCase(receiver.getAccountStatus())) {
            throw new RuntimeException(
                    "Receiver account is not active."
            );
        }

        // 8. Check balance
        if (sender.getBalance() < request.getAmount()) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available balance is ₹"
                            + sender.getBalance()
            );
        }

        // 9. Calculate new balances
        Double senderNewBalance =
                sender.getBalance() - request.getAmount();

        Double receiverNewBalance =
                receiver.getBalance() + request.getAmount();

        // 10. Update balances
        sender.setBalance(senderNewBalance);
        receiver.setBalance(receiverNewBalance);

        bankAccountRepository.save(sender);
        bankAccountRepository.save(receiver);

        // 11. Generate ONE transaction reference
        String transactionReference =
                "TXN" + System.currentTimeMillis();

        // ==========================================
        // 12. Sender transaction - DEBIT
        // ==========================================

        Transaction debitTransaction = new Transaction();

        debitTransaction.setTransactionReference(
                transactionReference
        );

        debitTransaction.setTransactionType(
                "TRANSFER_DEBIT"
        );

        debitTransaction.setAmount(
                request.getAmount()
        );

        debitTransaction.setBalanceAfterTransaction(
                senderNewBalance
        );

        debitTransaction.setStatus(
                "SUCCESS"
        );

        debitTransaction.setRemarks(
                request.getRemarks()
        );

        debitTransaction.setTransactionDate(
                LocalDateTime.now()
        );

        debitTransaction.setBankAccount(
                sender
        );

        transactionRepository.save(debitTransaction);

        // ==========================================
        // 13. Receiver transaction - CREDIT
        // ==========================================

        Transaction creditTransaction = new Transaction();

        creditTransaction.setTransactionReference(
                transactionReference
        );

        creditTransaction.setTransactionType(
                "TRANSFER_CREDIT"
        );

        creditTransaction.setAmount(
                request.getAmount()
        );

        creditTransaction.setBalanceAfterTransaction(
                receiverNewBalance
        );

        creditTransaction.setStatus(
                "SUCCESS"
        );

        creditTransaction.setRemarks(
                request.getRemarks()
        );

        creditTransaction.setTransactionDate(
                LocalDateTime.now()
        );

        creditTransaction.setBankAccount(
                receiver
        );

        transactionRepository.save(creditTransaction);

        // ==========================================
        // 14. Response
        // ==========================================

        TransferResponseDTO response =
                new TransferResponseDTO();

        response.setTransactionReference(
                transactionReference
        );

        response.setFromAccount(
                sender.getAccountNumber()
        );

        response.setToAccount(
                receiver.getAccountNumber()
        );

        response.setAmount(
                request.getAmount()
        );

        response.setSenderBalance(
                senderNewBalance
        );

        response.setReceiverBalance(
                receiverNewBalance
        );

        response.setMessage(
                "Money transferred successfully."
        );

        return response;
    }

    @Override
    public DepositResponseDTO depositMoney(DepositRequestDTO request) {

        // 1. Find Account
        BankAccount account = bankAccountRepository
                .findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with account number : "
                                        + request.getAccountNumber()));

        // 2. Validate
        if (request.getAmount() <= 0) {
            throw new InvalidAmountException(
                    "Deposit amount must be greater than zero.");
        }

        // 3. Update Balance
        Double previousBalance = account.getBalance();
        Double updatedBalance = previousBalance + request.getAmount();

        account.setBalance(updatedBalance);

        // 4. Save updated account
        bankAccountRepository.save(account);

        // 5. Create transaction
        Transaction transaction = new Transaction();

        transaction.setTransactionReference("TXN" + System.currentTimeMillis());
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(request.getAmount());
        transaction.setBalanceAfterTransaction(updatedBalance);
        transaction.setStatus("SUCCESS");
        transaction.setRemarks("Amount deposited successfully");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setBankAccount(account);

        // 6. Save transaction
        transactionRepository.save(transaction);


        DepositResponseDTO response = new DepositResponseDTO();
        response.setAccountNumber(account.getAccountNumber());
        response.setDepositedAmount(request.getAmount());
        response.setPreviousBalance(previousBalance);
        response.setUpdatedBalance(updatedBalance);
        response.setMessage("Amount deposited successfully.");

        return response;
    }

    @Override
    public WithdrawResponseDTO withdrawMoney(WithdrawRequestDTO request) {

        BankAccount account = bankAccountRepository
                .findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with account number : "
                                        + request.getAccountNumber()));

        if (request.getAmount() <= 0) {
            throw new InvalidAmountException(
                    "Withdrawal amount must be greater than zero.");
        }

        if (!account.getAccountStatus().equalsIgnoreCase("ACTIVE")) {
            throw new RuntimeException("Account is not active.");
        }

        if (account.getBalance() < request.getAmount()) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available balance is ₹"
                            + account.getBalance());
        }

        Double previousBalance = account.getBalance();

        Double updatedBalance = previousBalance - request.getAmount();

        account.setBalance(updatedBalance);

        bankAccountRepository.save(account);

        Transaction transaction = new Transaction();

        transaction.setTransactionReference("TXN" + System.currentTimeMillis());
        transaction.setTransactionType("WITHDRAW");
        transaction.setAmount(request.getAmount());
        transaction.setBalanceAfterTransaction(updatedBalance);
        transaction.setStatus("SUCCESS");
        transaction.setRemarks("Amount withdrawn successfully");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setBankAccount(account);

        // 6. Save transaction
        transactionRepository.save(transaction);

        // 7. Return response DTO
        WithdrawResponseDTO response = new WithdrawResponseDTO();

        response.setAccountNumber(account.getAccountNumber());
        response.setWithdrawnAmount(request.getAmount());
        response.setPreviousBalance(previousBalance);
        response.setUpdatedBalance(updatedBalance);
        response.setMessage("Amount withdrawn successfully.");

        return response;
    }

}
