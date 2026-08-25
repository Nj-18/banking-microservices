package com.banking.transaction_service.controller;

import com.banking.transaction_service.dto.StatementResponseDTO;
import com.banking.transaction_service.dto.TransactionRequestDTO;
import com.banking.transaction_service.dto.TransactionResponseDTO;
import com.banking.transaction_service.dto.TransferRequestDTO;
import com.banking.transaction_service.dto.TransferResponseDTO;
import com.banking.transaction_service.entity.Transaction;
import com.banking.transaction_service.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/statement")
    public StatementResponseDTO getStatement(
            @RequestParam String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return transactionService.getStatement(
                accountNumber,
                fromDate,
                toDate,
                page,
                size);
    }

    @GetMapping("/{accountNumber}")
    public List<TransactionResponseDTO> getTransactions(
            @PathVariable String accountNumber) {

        return transactionService.getTransactions(accountNumber);
    }

    @PostMapping
    public Transaction recordTransaction(@RequestBody TransactionRequestDTO request) {
        return transactionService.saveTransaction(request);
    }

    @PostMapping("/transfer")
    public TransferResponseDTO transferDetails(@RequestBody TransferRequestDTO requestDTO) {
        return transactionService.transferMoney(requestDTO);
    }
}
