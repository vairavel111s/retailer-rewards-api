package com.retailer.rewards.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class RewardCalculator {

    private static final BigDecimal FIRST_THRESHOLD =
            new BigDecimal("50");

    private static final BigDecimal SECOND_THRESHOLD =
            new BigDecimal("100");

    private static final BigDecimal SECOND_TIER_MULTIPLIER =
            new BigDecimal("2");

    /**
     * Calculates points for one purchase using whole dollars only.
     */
    public long calculatePoints(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException(
                    "Transaction amount must not be null");
        }

        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must not be negative");
        }

        BigDecimal wholeDollars =
                amount.setScale(0, RoundingMode.DOWN);

        if (wholeDollars.compareTo(FIRST_THRESHOLD) <= 0) {
            return 0L;
        }

        if (wholeDollars.compareTo(SECOND_THRESHOLD) <= 0) {
            return wholeDollars.subtract(FIRST_THRESHOLD)
                    .longValueExact();
        }

        return wholeDollars.subtract(SECOND_THRESHOLD)
                .multiply(SECOND_TIER_MULTIPLIER)
                .add(SECOND_THRESHOLD.subtract(FIRST_THRESHOLD))
                .longValueExact();
    }
}