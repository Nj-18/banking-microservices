package com.banking.customer_service.repository;

import com.banking.customer_service.entity.Customer;
//import com.banking.demo.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {

}
