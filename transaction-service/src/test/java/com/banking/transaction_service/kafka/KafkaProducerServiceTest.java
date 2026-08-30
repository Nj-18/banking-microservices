package com.banking.transaction_service.kafka;

import com.banking.transaction_service.config.KafkaProducerConfig;
import com.banking.transaction_service.event.TransactionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaProducerServiceTest {

    @Test
    void sendTransactionEvent_shouldPublishToTransactionEventsTopic() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, TransactionEvent> kafkaTemplate = mock(KafkaTemplate.class);
        CompletableFuture<SendResult<String, TransactionEvent>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(
                eq(KafkaProducerConfig.TRANSACTION_EVENTS_TOPIC),
                eq("TXN-1"),
                org.mockito.ArgumentMatchers.any(TransactionEvent.class)))
                .thenReturn(future);

        KafkaProducerService producer = new KafkaProducerService(kafkaTemplate);
        TransactionEvent event = sampleEvent();

        producer.sendTransactionEvent(event);

        verify(kafkaTemplate).send(
                KafkaProducerConfig.TRANSACTION_EVENTS_TOPIC,
                "TXN-1",
                event);
    }

    @Test
    void transactionEvent_shouldSerializeLocalDateTimeWithSpringObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonSerializer<TransactionEvent> serializer = new JsonSerializer<>(objectMapper);
        serializer.setAddTypeInfo(false);

        assertDoesNotThrow(() -> serializer.serialize(
                KafkaProducerConfig.TRANSACTION_EVENTS_TOPIC,
                sampleEvent()));
    }

    private TransactionEvent sampleEvent() {
        TransactionEvent event = new TransactionEvent();
        event.setTransactionReference("TXN-1");
        event.setFromAccountNumber("ACC1001");
        event.setToAccountNumber("ACC1002");
        event.setAmount(BigDecimal.valueOf(100));
        event.setTransactionType("TRANSFER");
        event.setStatus("SUCCESS");
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
}
