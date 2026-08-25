package com.banking.demo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferResponseDTO {
    private String transactionReference;

    private String fromAccount;

    private String toAccount;

    private Double amount;

    private Double senderBalance;

    private Double receiverBalance;

    private String message;
}
