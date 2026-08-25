package com.banking.account_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBankAccountRequestDTO {
    private Long customerId;

    private String accountType;

    private Double openingBalance;
}
