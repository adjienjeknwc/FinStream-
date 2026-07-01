package com.finstream.loan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finstream.loan.event.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanAuditProducerTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;
    private LoanAuditProducer producer;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        objectMapper = new ObjectMapper();
        producer = new LoanAuditProducer(kafkaTemplate, objectMapper);
    }

    @Test
    void testSendTransactionEvent() throws Exception {
        TransactionEvent event = new TransactionEvent(
                "tx-123",
                "acc-456",
                new BigDecimal("1500.50"),
                "DEPOSIT",
                1672531199000L
        );

        producer.sendTransactionEvent(event);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(eq("loan-audit-stream"), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals("tx-123", keyCaptor.getValue());
        String payload = payloadCaptor.getValue();
        assertTrue(payload.contains("\"transactionId\":\"tx-123\""));
        assertTrue(payload.contains("\"accountId\":\"acc-456\""));
        assertTrue(payload.contains("\"amount\":1500.50"));
        assertTrue(payload.contains("\"type\":\"DEPOSIT\""));
        assertTrue(payload.contains("\"timestamp\":1672531199000"));
    }
}
