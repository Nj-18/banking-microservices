package com.banking.api_gateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

    @RequestMapping("/customer")
    public ResponseEntity<Map<String, Object>> customerFallback() {
        return unavailable("Customer service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/account")
    public ResponseEntity<Map<String, Object>> accountFallback() {
        return unavailable("Account service is currently unavailable. Please try again later.");
    }

    @RequestMapping("/transaction")
    public ResponseEntity<Map<String, Object>> transactionFallback() {
        return unavailable("Transaction service is currently unavailable. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> unavailable(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
