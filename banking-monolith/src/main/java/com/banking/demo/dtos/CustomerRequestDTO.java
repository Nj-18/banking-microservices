package com.banking.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDTO {

        private String firstName;

        private String lastName;

        private String email;

        private String mobileNumber;

        private String customerStatus;
}
