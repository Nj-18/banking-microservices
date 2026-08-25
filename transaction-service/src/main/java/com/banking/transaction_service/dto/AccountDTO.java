package com.banking.transaction_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDTO {
    private Long id;
    private String accountNumber;
    private String accountType;
    private Double balance;
    private String accountStatus;
    private Long customerId;
}
