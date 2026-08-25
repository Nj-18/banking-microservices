package com.banking.account_service;

import com.banking.account_service.client.CustomerClient;
import com.banking.account_service.client.TransactionClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class AccountServiceApplicationTests {

	@MockitoBean
	private CustomerClient customerClient;

	@MockitoBean
	private TransactionClient transactionClient;

	@Test
	void contextLoads() {
	}

}
