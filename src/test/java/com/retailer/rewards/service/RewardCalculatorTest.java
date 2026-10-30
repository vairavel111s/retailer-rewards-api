package com.retailer.rewards.service;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RewardCalculatorTest {

    private final RewardCalculator calculator = new RewardCalculator();

    @ParameterizedTest(name = "${0} earns {1} points")
    @CsvSource({
            "0, 0",
            "49.99, 0",
            "50, 0",
            "50.99, 0",
            "51, 1",
            "75, 25",
            "99.99, 49",
            "100, 50",
            "100.99, 50",
            "101, 52",
            "120, 90",
            "120.75, 90",
            "150, 150"
    })
    void shouldCalculatePointsForEachPurchase(
            String amount, long expectedPoints) {

        long actualPoints =
                calculator.calculatePoints(new BigDecimal(amount));

        assertEquals(expectedPoints, actualPoints);
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePoints(null));
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculatePoints(
                        new BigDecimal("-0.01")));
    }

    @Test
    void shouldCalculatePurchasesSeparately() {
        long firstPurchasePoints =
                calculator.calculatePoints(new BigDecimal("60"));

        long secondPurchasePoints =
                calculator.calculatePoints(new BigDecimal("60"));

        assertEquals(20L, firstPurchasePoints + secondPurchasePoints);
    }

    @Test
    void shouldRejectPointsExceedingLongRange() {
        assertThrows(
                ArithmeticException.class,
                () -> calculator.calculatePoints(
                        new BigDecimal("9223372036854775808")));
    }
}