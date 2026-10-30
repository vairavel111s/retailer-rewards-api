package com.retailer.rewards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TransactionDetail {

    private final long transactionId;
    private final LocalDate date;
    private final BigDecimal amount;
    private final long rewardPoints;

    public TransactionDetail(long transactionId,
                             LocalDate date,
                             BigDecimal amount,
                             long rewardPoints) {
        this.transactionId = transactionId;
        this.date = date;
        this.amount = amount;
        this.rewardPoints = rewardPoints;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public long getRewardPoints() {
        return rewardPoints;
    }
}