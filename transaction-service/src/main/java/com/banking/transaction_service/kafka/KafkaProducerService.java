package com.banking.transaction_service.kafka;

import com.banking.transaction_service.event.TransactionEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "transaction-events";

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public KafkaProducerService(
            KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransactionEvent(TransactionEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.getTransactionReference(),
                event
        ).whenComplete((result, ex) -> {

            if (ex != null) {
                System.err.println(
                        "❌ Kafka send failed: " + ex.getMessage()
                );
            } else {
                System.out.println(
                        "✅ Kafka message sent successfully. " +
                                "Topic: " + result.getRecordMetadata().topic() +
                                ", Partition: " + result.getRecordMetadata().partition() +
                                ", Offset: " + result.getRecordMetadata().offset()
                );
            }
        });
    }
}