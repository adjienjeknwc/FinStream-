package com.finstream.loan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finstream.loan.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoanAuditProducer {

    private static final Logger log = LoggerFactory.getLogger(LoanAuditProducer.class);
    private static final String TOPIC = "loan-audit-stream";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public LoanAuditProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendTransactionEvent(TransactionEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            log.info("Sending transaction event to topic {}: {}", TOPIC, payload);
            kafkaTemplate.send(TOPIC, event.transactionId(), payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize transaction event", e);
            throw new RuntimeException("Failed to serialize TransactionEvent", e);
        }
    }
}
