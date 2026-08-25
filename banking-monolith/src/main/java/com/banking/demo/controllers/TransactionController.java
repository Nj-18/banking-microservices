package com.banking.demo.controllers;

import com.banking.demo.dtos.StatementResponseDTO;
import com.banking.demo.dtos.TransactionResponseDTO;
import com.banking.demo.dtos.TransferRequestDTO;
import com.banking.demo.dtos.TransferResponseDTO;
import com.banking.demo.services.TransactionService;
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

    @GetMapping("/{accountNumber}")
    public List<TransactionResponseDTO> getTransactions(
            @PathVariable String accountNumber) {

        return transactionService.getTransactions(accountNumber);
    }

    @PostMapping("/transfer")
    public TransferResponseDTO transferDetails(@RequestBody TransferRequestDTO requestDTO)
    {
        return transactionService.transferMoney(requestDTO);
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
}