package com.retailer.rewards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CustomerRewardResponse {

    private final long customerId;
    private final String customerName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<MonthlyRewardSummary> monthlyRewards;
    private final List<TransactionDetail> transactions;
    private final BigDecimal totalSpent;
    private final long totalRewardPoints;

    public CustomerRewardResponse(
            long customerId,
            String customerName,
            LocalDate startDate,
            LocalDate endDate,
            List<MonthlyRewardSummary> monthlyRewards,
            List<TransactionDetail> transactions,
            BigDecimal totalSpent,
            long totalRewardPoints) {

        this.customerId = customerId;
        this.customerName = customerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthlyRewards = Collections.unmodifiableList(
                new ArrayList<>(monthlyRewards));
        this.transactions = Collections.unmodifiableList(
                new ArrayList<>(transactions));
        this.totalSpent = totalSpent;
        this.totalRewardPoints = totalRewardPoints;
    }

    public long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<MonthlyRewardSummary> getMonthlyRewards() {
        return monthlyRewards;
    }

    public List<TransactionDetail> getTransactions() {
        return transactions;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public long getTotalRewardPoints() {
        return totalRewardPoints;
    }
}