package com.banking.customer_service.serviceImpl;//package com.banking.demo.serviceImpl;

//import com.banking.demo.entities.Customer;
//import com.banking.demo.exceptions.CustomerNotFoundException;
//import com.banking.demo.repositories.CustomerRepository;
//import com.banking.demo.services.CustomerService;
import com.banking.customer_service.entity.Customer;
import com.banking.customer_service.exception.CustomerNotFoundException;
import com.banking.customer_service.repository.CustomerRepository;
import com.banking.customer_service.service.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer saveCustomer(Customer customer) {

        return customerRepository.save(customer);

    }

    @Override
    public List<Customer> getAllCustomers()
    {
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomerById(Long id) {

        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer not found with id : " + id));
    }

}
