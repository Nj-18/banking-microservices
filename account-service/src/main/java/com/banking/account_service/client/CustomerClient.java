package com.banking.account_service.client;

import com.banking.account_service.dto.CustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CUSTOMER-SERVICE")
public interface CustomerClient {

    @GetMapping("/api/customer/{id}")
    CustomerDTO getCustomer(@PathVariable Long id);
}