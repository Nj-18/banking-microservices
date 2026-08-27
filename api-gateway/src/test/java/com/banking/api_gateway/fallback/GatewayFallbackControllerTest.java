package com.banking.api_gateway.fallback;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayFallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void customerFallback_returns503() throws Exception {
        mockMvc.perform(get("/fallback/customer"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(
                        "Customer service is currently unavailable. Please try again later."));
    }

    @Test
    void accountFallback_returns503() throws Exception {
        mockMvc.perform(post("/fallback/account"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void transactionFallback_returns503() throws Exception {
        mockMvc.perform(get("/fallback/transaction"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(
                        "Transaction service is currently unavailable. Please try again later."));
    }
}
