package com.banking.demo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequestDTO {

    private String fromAccountNumber;

    private String toAccountNumber;

    private Double amount;

    private String remarks;
}