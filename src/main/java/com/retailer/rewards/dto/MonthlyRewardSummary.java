package com.retailer.rewards.dto;

import java.math.BigDecimal;

public final class MonthlyRewardSummary {

    private final String month;
    private final int transactionCount;
    private final BigDecimal totalSpent;
    private final long rewardPoints;

    public MonthlyRewardSummary(String month,
                                int transactionCount,
                                BigDecimal totalSpent,
                                long rewardPoints) {
        this.month = month;
        this.transactionCount = transactionCount;
        this.totalSpent = totalSpent;
        this.rewardPoints = rewardPoints;
    }

    public String getMonth() {
        return month;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public long getRewardPoints() {
        return rewardPoints;
    }
}