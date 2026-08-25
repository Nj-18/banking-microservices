package com.banking.customer_service.service;

//import com.banking.demo.entities.Customer;

import com.banking.customer_service.entity.Customer;

import java.util.List;

public interface CustomerService {
    Customer saveCustomer(Customer customer);
    List<Customer> getAllCustomers();
    Customer getCustomerById(Long id);
}
