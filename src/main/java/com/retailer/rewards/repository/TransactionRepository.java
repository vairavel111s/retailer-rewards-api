package com.retailer.rewards.repository;

import com.retailer.rewards.model.Transaction;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TransactionRepository {

    private final List<Transaction> transactions =
            Collections.unmodifiableList(Arrays.asList(
                    transaction(1, 101, "2026-07-01", "120"),
                    transaction(2, 101, "2026-07-15", "75"),
                    transaction(3, 101, "2026-08-10", "100"),
                    transaction(4, 101, "2026-08-20", "50"),
                    transaction(5, 101, "2026-09-05", "120.75"),
                    transaction(6, 101, "2026-09-30", "60"),

                    transaction(7, 102, "2026-07-12", "150"),
                    transaction(8, 102, "2026-07-25", "60"),
                    transaction(9, 102, "2026-09-18", "101"),

                    transaction(10, 101, "2026-06-30", "200"),
                    transaction(11, 101, "2026-10-01", "200")
            ));

    public List<Transaction> findByCustomerIdAndDateRange(
            long customerId, LocalDate startDate, LocalDate endDate) {

        if (customerId <= 0) {
            throw new IllegalArgumentException(
                    "Customer ID must be positive");
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date must not be null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must not be after end date");
        }

        return transactions.stream()
                .filter(transaction ->
                        transaction.getCustomerId() == customerId)
                .filter(transaction ->
                        !transaction.getDate().isBefore(startDate))
                .filter(transaction ->
                        !transaction.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    private static Transaction transaction(
            long id, long customerId, String date, String amount) {

        return new Transaction(
                id,
                customerId,
                new BigDecimal(amount),
                LocalDate.parse(date));
    }
}