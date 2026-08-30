package com.banking.transaction_service.kafka;

import com.banking.transaction_service.config.KafkaProducerConfig;
import com.banking.transaction_service.event.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransactionEvent(TransactionEvent event) {
        kafkaTemplate.send(
                        KafkaProducerConfig.TRANSACTION_EVENTS_TOPIC,
                        event.getTransactionReference(),
                        event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error(
                                "Kafka send failed for ref={} topic={}",
                                event.getTransactionReference(),
                                KafkaProducerConfig.TRANSACTION_EVENTS_TOPIC,
                                ex);
                    } else {
                        log.info(
                                "Kafka message sent. topic={} partition={} offset={} ref={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                event.getTransactionReference());
                    }
                });
    }
}
