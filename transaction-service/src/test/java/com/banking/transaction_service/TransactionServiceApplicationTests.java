package com.banking.transaction_service;

import com.banking.transaction_service.client.AccountClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TransactionServiceApplicationTests {

	@MockitoBean
	private AccountClient accountClient;

	@Test
	void contextLoads() {
	}

}
