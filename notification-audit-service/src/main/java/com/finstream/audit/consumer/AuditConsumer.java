package com.finstream.audit.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditConsumer.class);

    @KafkaListener(topics = "loan-audit-stream", groupId = "audit-group")
    public void consume(String payload) {
        log.info("Received message payload: {}", payload);
        if (payload != null && payload.contains("poison-pill")) {
            log.warn("Poison-pill detected in payload, throwing exception to trigger DLT fallback!");
            throw new IllegalArgumentException("Invalid payload: Contains poison-pill marker");
        }
        log.info("Message processed successfully: {}", payload);
    }
}
