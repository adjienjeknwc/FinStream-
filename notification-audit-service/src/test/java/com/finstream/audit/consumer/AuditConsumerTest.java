package com.finstream.audit.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditConsumerTest {

    private AuditConsumer auditConsumer;

    @BeforeEach
    void setUp() {
        auditConsumer = new AuditConsumer();
    }

    @Test
    void testConsumeCleanPayload() {
        String cleanPayload = "{\"transactionId\":\"tx-123\",\"accountId\":\"acc-456\",\"amount\":1500.50,\"type\":\"DEPOSIT\",\"timestamp\":1672531199000}";
        assertDoesNotThrow(() -> auditConsumer.consume(cleanPayload));
    }

    @Test
    void testConsumePoisonPillPayloadThrowsException() {
        String poisonPayload = "{\"transactionId\":\"tx-999\",\"accountId\":\"acc-error\",\"amount\":0.00,\"type\":\"poison-pill\",\"timestamp\":1672531199000}";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> auditConsumer.consume(poisonPayload));
        assertTrue(exception.getMessage().contains("Contains poison-pill marker"));
    }
}
