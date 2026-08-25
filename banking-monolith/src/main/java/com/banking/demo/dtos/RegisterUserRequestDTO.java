package com.banking.demo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;

    private String username;
    private String password;
}
