package com.retailer.rewards.service;

import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TransactionFetchService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionFetchService.class);

    private final TransactionRepository transactionRepository;

    public TransactionFetchService(
            TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Simulates asynchronous data retrieval using an in-memory source.
     */
    @Async("transactionExecutor")
    public CompletableFuture<List<Transaction>> fetchTransactions(
            long customerId, LocalDate startDate, LocalDate endDate) {

        LOGGER.debug(
                "Fetching transactions for customer {} from {} to {}",
                customerId, startDate, endDate);

        List<Transaction> transactions =
                transactionRepository.findByCustomerIdAndDateRange(
                        customerId, startDate, endDate);

        LOGGER.debug(
                "Fetched {} transactions for customer {}",
                transactions.size(), customerId);

        return CompletableFuture.completedFuture(transactions);
    }
}