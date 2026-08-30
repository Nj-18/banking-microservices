package com.banking.transaction_service.controller;

import com.banking.transaction_service.client.AccountClient;
import com.banking.transaction_service.kafka.KafkaProducerService;
import com.banking.transaction_service.dto.AccountDTO;
import com.banking.transaction_service.dto.TransferRequestDTO;
import com.banking.transaction_service.dto.TransferResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountClient accountClient;

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    @Test
    void recordHistoryStatementAndTransfer_endToEndThroughHttp() throws Exception {
        AccountDTO account = new AccountDTO();
        account.setId(1L);
        account.setAccountNumber("ACC1001");
        account.setBalance(6500.0);
        account.setAccountStatus("ACTIVE");
        when(accountClient.getAccount("ACC1001")).thenReturn(account);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountNumber": "ACC1001",
                                  "transactionReference": "TXN-DEP-1",
                                  "transactionType": "DEPOSIT",
                                  "amount": 1500.0,
                                  "balanceAfterTransaction": 6500.0,
                                  "remarks": "Amount deposited successfully"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference").value("TXN-DEP-1"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(get("/api/transactions/ACC1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$[0].amount").value(1500.0));

        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/transactions/statement")
                        .param("accountNumber", "ACC1001")
                        .param("fromDate", today.minusDays(1).toString())
                        .param("toDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC1001"))
                .andExpect(jsonPath("$.currentBalance").value(6500.0))
                .andExpect(jsonPath("$.transactions", hasSize(1)))
                .andExpect(jsonPath("$.transactions[0].transactionReference").value("TXN-DEP-1"));

        TransferResponseDTO transferResponse = new TransferResponseDTO();
        transferResponse.setFromAccount("ACC1001");
        transferResponse.setToAccount("ACC1002");
        transferResponse.setAmount(250.0);
        transferResponse.setMessage("Money transferred successfully.");
        when(accountClient.transferMoney(any(TransferRequestDTO.class)))
                .thenReturn(transferResponse);

        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromAccount").value("ACC1001"))
                .andExpect(jsonPath("$.toAccount").value("ACC1002"))
                .andExpect(jsonPath("$.amount").value(250.0));
    }

    @Test
    void getTransactions_shouldReturn404WhenAccountMissing() throws Exception {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/account/MISSING",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate());
        when(accountClient.getAccount("MISSING"))
                .thenThrow(new FeignException.NotFound("not found", request, null, Collections.emptyMap()));

        mockMvc.perform(get("/api/transactions/MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Account not found with account number : MISSING"));
    }

    private TransferRequestDTO transferBody() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setFromAccountNumber("ACC1001");
        request.setToAccountNumber("ACC1002");
        request.setAmount(250.0);
        request.setRemarks("Rent");
        return request;
    }
}
