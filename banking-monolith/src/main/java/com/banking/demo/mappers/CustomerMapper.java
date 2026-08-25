package com.banking.demo.mappers;

import com.banking.demo.dtos.CustomerRequestDTO;
import com.banking.demo.entities.Customer;

public class CustomerMapper {

    public static Customer toEntity(CustomerRequestDTO dto) {

        Customer customer = new Customer();

        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setMobileNumber(dto.getMobileNumber());
        customer.setCustomerStatus(dto.getCustomerStatus());

        return customer;
    }
}
