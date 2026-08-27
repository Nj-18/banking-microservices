package com.banking.account_service.client;

import com.banking.account_service.dto.CustomerDTO;
import com.banking.account_service.exception.DownstreamServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerClientFallbackFactory implements FallbackFactory<CustomerClient> {

    @Override
    public CustomerClient create(Throwable cause) {
        return new CustomerClient() {
            @Override
            public CustomerDTO getCustomer(Long id) {
                log.error("Customer service is unavailable while fetching customer {}", id, cause);
                throw new DownstreamServiceException(
                        "Customer service is currently unavailable. Please try again later.");
            }
        };
    }
}
