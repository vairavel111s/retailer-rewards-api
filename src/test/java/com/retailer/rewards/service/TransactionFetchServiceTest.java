package com.retailer.rewards.service;

import com.retailer.rewards.config.AsyncConfig;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = {
        AsyncConfig.class,
        TransactionFetchService.class,
        TransactionFetchServiceTest.TestConfig.class
})
class TransactionFetchServiceTest {

    @Autowired
    private TransactionFetchService fetchService;

    @Autowired
    private TransactionRepository repository;

    private final LocalDate startDate = LocalDate.of(2026, 7, 1);
    private final LocalDate endDate = LocalDate.of(2026, 9, 30);

    @BeforeEach
    void setUp() {
        reset(repository);
    }

    @Test
    void shouldFetchTransactionsOnBackgroundThread() throws Exception {
        AtomicReference<Thread> executionThread =
                new AtomicReference<>();

        Thread callingThread = Thread.currentThread();

        Transaction transaction = new Transaction(
                1L, 101L, new BigDecimal("120"), startDate);

        when(repository.findByCustomerIdAndDateRange(
                101L, startDate, endDate))
                .thenAnswer(invocation -> {
                    executionThread.set(Thread.currentThread());
                    return Collections.singletonList(transaction);
                });

        List<Transaction> result = fetchService.fetchTransactions(
                101L, startDate, endDate).get(5, TimeUnit.SECONDS);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertNotNull(executionThread.get());
        assertNotSame(callingThread, executionThread.get());
        assertTrue(executionThread.get().getName()
                .startsWith("transaction-fetch-"));

        verify(repository).findByCustomerIdAndDateRange(
                101L, startDate, endDate);
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsExist()
            throws Exception {

        when(repository.findByCustomerIdAndDateRange(
                103L, startDate, endDate))
                .thenReturn(Collections.emptyList());

        List<Transaction> result = fetchService.fetchTransactions(
                103L, startDate, endDate).get(5, TimeUnit.SECONDS);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldPropagateRepositoryFailure() {
        IllegalStateException failure =
                new IllegalStateException("Data source unavailable");

        when(repository.findByCustomerIdAndDateRange(
                101L, startDate, endDate))
                .thenThrow(failure);

        ExecutionException exception = assertThrows(
                ExecutionException.class,
                () -> fetchService.fetchTransactions(
                                101L, startDate, endDate)
                        .get(5, TimeUnit.SECONDS));

        assertSame(failure, exception.getCause());
    }

    @Configuration
    static class TestConfig {

        @Bean
        TransactionRepository transactionRepository() {
            return mock(TransactionRepository.class);
        }
    }
}