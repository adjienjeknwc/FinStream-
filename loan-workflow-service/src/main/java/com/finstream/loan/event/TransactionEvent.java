package com.finstream.loan.event;

import java.math.BigDecimal;

public record TransactionEvent(
    String transactionId,
    String accountId,
    BigDecimal amount,
    String type,
    long timestamp
) {}
