package com.banking.demo.serviceImpl;

import com.banking.demo.entities.Customer;
import com.banking.demo.exceptions.CustomerNotFoundException;
import com.banking.demo.repositories.CustomerRepository;
import com.banking.demo.services.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

//import static jdk.internal.classfile.impl.verifier.VerifierImpl.verify;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerServiceImpl;

    @Test
    void saveCustomer_shouldReturnSavedCustomer() {
        // =========================
        // ARRANGE
        // =========================

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Nikunj");
        customer.setLastName("Jain");

        when(customerRepository.save(customer))
                .thenReturn((customer));

        // =========================
        // ACT
        // =========================

        Customer result = customerServiceImpl.saveCustomer(customer);

        // =========================
        // ASSERT
        // =========================

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Nikunj", result.getFirstName());

    }

    @Test
    void getAllCustomers_shouldReturnCustomers() {

        // =========================
        // ARRANGE
        // =========================

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Nikunj");
        customer.setLastName("Jain");

        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setFirstName("Rahul");
        customer2.setLastName("Sharma");

        Customer customer3 = new Customer();
        customer3.setId(3L);
        customer3.setFirstName("Amit");
        customer3.setLastName("Patel");

        List<Customer> list = List.of(customer,customer2,customer3);

        when(customerRepository.findAll())
                .thenReturn(list);

        // =========================
        // ACT
        // =========================

        List<Customer> result = customerServiceImpl.getAllCustomers();

        // =========================
        // ASSERT
        // =========================

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Nikunj", result.get(0).getFirstName());

        verify(customerRepository, times(1))
                .findAll();


    }

    @Test
    void getAllCustomers_shouldReturnEmptyList_whenNoCustomers() {

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Nikunj");
        customer.setLastName("Jain");

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        Customer customer1 = customerServiceImpl.getCustomerById(1L);
        assertNotNull(customer1);
        assertEquals(1L, customer1.getId());

        verify(customerRepository)
                .findById(1L);

    }

    @Test
    void getCustomerById_shouldThrowException_whenCustomerNotFound() {

        // ARRANGE
        Long customerId = 999L;

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        CustomerNotFoundException exception =
                assertThrows(
                        CustomerNotFoundException.class,
                        () -> customerServiceImpl
                                .getCustomerById(customerId)
                );

        // ASSERT
        assertEquals(
                "Customer not found with id : " + customerId,
                exception.getMessage()
        );
        }
}