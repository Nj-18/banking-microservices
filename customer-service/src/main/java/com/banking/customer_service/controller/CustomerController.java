package com.banking.customer_service.controller;

//import com.banking.demo.dtos.CustomerRequestDTO;
//import com.banking.demo.entities.Customer;
//import com.banking.demo.mappers.CustomerMapper;
//import com.banking.demo.serviceImpl.CustomerServiceImpl;
import com.banking.customer_service.dto.CustomerRequestDTO;
import com.banking.customer_service.entity.Customer;
import com.banking.customer_service.mapper.CustomerMapper;
import com.banking.customer_service.serviceImpl.CustomerServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    private final CustomerServiceImpl customerService;

    public CustomerController(CustomerServiceImpl customerService) {
        this.customerService = customerService;
    }


    @PostMapping("")
    Customer registerCustomer(@RequestBody CustomerRequestDTO dto)
    {
        Customer customer = CustomerMapper.toEntity(dto);
        return customerService.saveCustomer(customer);
    }

    @GetMapping("")
    List<Customer> getAllCustomers()
    {
        return customerService.getAllCustomers();

    }

    @GetMapping("/{id}")
    Customer getCustomer(@PathVariable Long id)
    {
        return customerService.getCustomerById(id);

    }
}
