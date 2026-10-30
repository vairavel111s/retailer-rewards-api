package com.retailer.rewards.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Transaction {

    private final long id;
    private final long customerId;
    private final BigDecimal amount;
    private final LocalDate date;

    public Transaction(long id, long customerId,
                       BigDecimal amount, LocalDate date) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Transaction ID must be positive");
        }

        if (customerId <= 0) {
            throw new IllegalArgumentException(
                    "Customer ID must be positive");
        }

        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must not be null or negative");
        }

        if (date == null) {
            throw new IllegalArgumentException(
                    "Transaction date must not be null");
        }

        this.id = id;
        this.customerId = customerId;
        this.amount = amount;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }
}