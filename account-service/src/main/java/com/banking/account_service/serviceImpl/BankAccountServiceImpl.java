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
import com.banking.account_service.service.BankAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final CustomerClient customerClient;
    private final TransactionClient transactionClient;

    public BankAccountServiceImpl(
            BankAccountRepository bankAccountRepository,
            CustomerClient customerClient,
            TransactionClient transactionClient) {

        this.bankAccountRepository = bankAccountRepository;
        this.customerClient = customerClient;
        this.transactionClient = transactionClient;
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

        return bankAccountRepository.save(bankAccount);
    }

    @Override
    public BankAccount getAccountByAccountNumber(String accountNumber) {
        return bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with account number : "
                                        + accountNumber));
    }

    @Transactional
    @Override
    public TransferResponseDTO transferMoney(TransferRequestDTO request) {

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new InvalidAmountException(
                    "Transfer amount must be greater than zero."
            );
        }

        if (request.getFromAccountNumber() == null
                || request.getToAccountNumber() == null) {

            throw new RuntimeException(
                    "From account and To account are required."
            );
        }

        if (request.getFromAccountNumber()
                .equals(request.getToAccountNumber())) {

            throw new RuntimeException(
                    "Source and destination accounts cannot be the same."
            );
        }

        BankAccount sender = bankAccountRepository
                .findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Sender account not found: "
                                        + request.getFromAccountNumber()
                        )
                );

        BankAccount receiver = bankAccountRepository
                .findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Receiver account not found: "
                                        + request.getToAccountNumber()
                        )
                );

        if (!"ACTIVE".equalsIgnoreCase(sender.getAccountStatus())) {
            throw new RuntimeException(
                    "Sender account is not active."
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(receiver.getAccountStatus())) {
            throw new RuntimeException(
                    "Receiver account is not active."
            );
        }

        if (sender.getBalance() < request.getAmount()) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available balance is ₹"
                            + sender.getBalance()
            );
        }

        Double senderNewBalance =
                sender.getBalance() - request.getAmount();

        Double receiverNewBalance =
                receiver.getBalance() + request.getAmount();

        sender.setBalance(senderNewBalance);
        receiver.setBalance(receiverNewBalance);

        bankAccountRepository.save(sender);
        bankAccountRepository.save(receiver);

        String transactionReference =
                "TXN" + System.currentTimeMillis();

        recordTransaction(
                sender.getAccountNumber(),
                transactionReference,
                "TRANSFER_DEBIT",
                request.getAmount(),
                senderNewBalance,
                request.getRemarks());

        recordTransaction(
                receiver.getAccountNumber(),
                transactionReference,
                "TRANSFER_CREDIT",
                request.getAmount(),
                receiverNewBalance,
                request.getRemarks());

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

        BankAccount account = bankAccountRepository
                .findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with account number : "
                                        + request.getAccountNumber()));

        if (request.getAmount() <= 0) {
            throw new InvalidAmountException(
                    "Deposit amount must be greater than zero.");
        }

        Double previousBalance = account.getBalance();
        Double updatedBalance = previousBalance + request.getAmount();

        account.setBalance(updatedBalance);

        bankAccountRepository.save(account);

        recordTransaction(
                account.getAccountNumber(),
                "TXN" + System.currentTimeMillis(),
                "DEPOSIT",
                request.getAmount(),
                updatedBalance,
                "Amount deposited successfully");

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

        recordTransaction(
                account.getAccountNumber(),
                "TXN" + System.currentTimeMillis(),
                "WITHDRAW",
                request.getAmount(),
                updatedBalance,
                "Amount withdrawn successfully");

        WithdrawResponseDTO response = new WithdrawResponseDTO();

        response.setAccountNumber(account.getAccountNumber());
        response.setWithdrawnAmount(request.getAmount());
        response.setPreviousBalance(previousBalance);
        response.setUpdatedBalance(updatedBalance);
        response.setMessage("Amount withdrawn successfully.");

        return response;
    }

    private void recordTransaction(
            String accountNumber,
            String transactionReference,
            String transactionType,
            Double amount,
            Double balanceAfterTransaction,
            String remarks) {

        TransactionRequestDTO request = new TransactionRequestDTO();
        request.setAccountNumber(accountNumber);
        request.setTransactionReference(transactionReference);
        request.setTransactionType(transactionType);
        request.setAmount(amount);
        request.setBalanceAfterTransaction(balanceAfterTransaction);
        request.setRemarks(remarks);

        transactionClient.recordTransaction(request);
    }

}
