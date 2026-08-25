package com.banking.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponseDTO {
    private String message;
    private String accountNumber;
    private Double previousBalance;
    private Double depositedAmount;
    private Double updatedBalance;
}
