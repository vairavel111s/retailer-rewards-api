package com.retailer.rewards.repository;

import com.retailer.rewards.model.Transaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRepositoryTest {

    private final TransactionRepository repository =
            new TransactionRepository();

    private final LocalDate startDate = LocalDate.of(2026, 7, 1);
    private final LocalDate endDate = LocalDate.of(2026, 9, 30);

    @Test
    void shouldReturnOnlyRequestedCustomerTransactionsWithinRange() {
        List<Transaction> result =
                repository.findByCustomerIdAndDateRange(
                        101L, startDate, endDate);

        assertEquals(6, result.size());

        assertTrue(result.stream().allMatch(
                transaction -> transaction.getCustomerId() == 101L));

        assertEquals(
                java.util.Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L),
                result.stream()
                        .map(Transaction::getId)
                        .collect(Collectors.toList()));
    }

    @Test
    void shouldIncludeStartDate() {
        List<Transaction> result =
                repository.findByCustomerIdAndDateRange(
                        101L, startDate, startDate);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void shouldIncludeEndDate() {
        List<Transaction> result =
                repository.findByCustomerIdAndDateRange(
                        101L, endDate, endDate);

        assertEquals(1, result.size());
        assertEquals(6L, result.get(0).getId());
    }

    @Test
    void shouldReturnAnotherCustomerTransactionsSeparately() {
        List<Transaction> result =
                repository.findByCustomerIdAndDateRange(
                        102L, startDate, endDate);

        assertEquals(
                java.util.Arrays.asList(7L, 8L, 9L),
                result.stream()
                        .map(Transaction::getId)
                        .collect(Collectors.toList()));
    }

    @Test
    void shouldReturnEmptyListForCustomerWithoutTransactions() {
        assertTrue(repository.findByCustomerIdAndDateRange(
                103L, startDate, endDate).isEmpty());
    }

    @Test
    void shouldReturnEmptyListForMonthWithoutPurchases() {
        assertTrue(repository.findByCustomerIdAndDateRange(
                102L,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)).isEmpty());
    }

    @Test
    void shouldRejectInvalidCustomerId() {
        assertThrows(IllegalArgumentException.class,
                () -> repository.findByCustomerIdAndDateRange(
                        0L, startDate, endDate));
    }

    @Test
    void shouldRejectNullStartDate() {
        assertThrows(IllegalArgumentException.class,
                () -> repository.findByCustomerIdAndDateRange(
                        101L, null, endDate));
    }

    @Test
    void shouldRejectNullEndDate() {
        assertThrows(IllegalArgumentException.class,
                () -> repository.findByCustomerIdAndDateRange(
                        101L, startDate, null));
    }

    @Test
    void shouldRejectReversedDateRange() {
        assertThrows(IllegalArgumentException.class,
                () -> repository.findByCustomerIdAndDateRange(
                        101L, endDate, startDate));
    }
}