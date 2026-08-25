package com.banking.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawResponseDTO {

    private String accountNumber;

    private Double withdrawnAmount;

    private Double previousBalance;

    private Double updatedBalance;

    private String message;
}