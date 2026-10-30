package com.retailer.rewards.service;

import com.retailer.rewards.dto.CustomerRewardResponse;
import com.retailer.rewards.dto.MonthlyRewardSummary;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.InvalidDateRangeException;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RewardServiceTest {

    private TransactionFetchService fetchService;
    private RewardService rewardService;

    private final LocalDate startDate = LocalDate.of(2026, 7, 1);
    private final LocalDate endDate = LocalDate.of(2026, 9, 30);

    @BeforeEach
    void setUp() {
        fetchService = mock(TransactionFetchService.class);

        rewardService = new RewardService(
                new CustomerRepository(),
                fetchService,
                new RewardCalculator());
    }

    @Test
    void shouldCalculateMonthlyAndOverallRewards() throws Exception {
        stubTransactions(101L, startDate, endDate);

        CustomerRewardResponse response =
                rewardService.getCustomerRewards(
                                101L, startDate, endDate)
                        .get(5, TimeUnit.SECONDS);

        assertEquals(101L, response.getCustomerId());
        assertEquals("Alice Morgan", response.getCustomerName());
        assertEquals(startDate, response.getStartDate());
        assertEquals(endDate, response.getEndDate());
        assertEquals(6, response.getTransactions().size());
        assertEquals(3, response.getMonthlyRewards().size());
        assertEquals(265L, response.getTotalRewardPoints());

        assertEquals(0, new BigDecimal("525.75")
                .compareTo(response.getTotalSpent()));

        assertMonth(response.getMonthlyRewards().get(0),
                "2026-07", 2, "195", 115L);
        assertMonth(response.getMonthlyRewards().get(1),
                "2026-08", 2, "150", 50L);
        assertMonth(response.getMonthlyRewards().get(2),
                "2026-09", 2, "180.75", 100L);
    }

    @Test
    void shouldIncludeMonthWithoutPurchases() throws Exception {
        stubTransactions(102L, startDate, endDate);

        CustomerRewardResponse response =
                rewardService.getCustomerRewards(
                                102L, startDate, endDate)
                        .get(5, TimeUnit.SECONDS);

        assertEquals(212L, response.getTotalRewardPoints());

        assertMonth(response.getMonthlyRewards().get(1),
                "2026-08", 0, "0", 0L);
    }

    @Test
    void shouldReturnZeroRewardsForCustomerWithoutPurchases()
            throws Exception {

        stubTransactions(103L, startDate, endDate);

        CustomerRewardResponse response =
                rewardService.getCustomerRewards(
                                103L, startDate, endDate)
                        .get(5, TimeUnit.SECONDS);

        assertTrue(response.getTransactions().isEmpty());
        assertEquals(0L, response.getTotalRewardPoints());
        assertEquals(0, BigDecimal.ZERO
                .compareTo(response.getTotalSpent()));
        assertEquals(3, response.getMonthlyRewards().size());

        for (MonthlyRewardSummary month : response.getMonthlyRewards()) {
            assertEquals(0, month.getTransactionCount());
            assertEquals(0L, month.getRewardPoints());
        }
    }

    @Test
    void shouldCalculateOnlyPurchasesWithinPartialMonth()
            throws Exception {

        LocalDate from = LocalDate.of(2026, 7, 10);
        LocalDate to = LocalDate.of(2026, 7, 20);
        stubTransactions(101L, from, to);

        CustomerRewardResponse response =
                rewardService.getCustomerRewards(101L, from, to)
                        .get(5, TimeUnit.SECONDS);

        assertEquals(1, response.getTransactions().size());
        assertEquals(2L,
                response.getTransactions().get(0).getTransactionId());

        assertMonth(response.getMonthlyRewards().get(0),
                "2026-07", 1, "75", 25L);
    }

    @Test
    void shouldRejectUnknownCustomerBeforeFetching() {
        assertThrows(CustomerNotFoundException.class,
                () -> rewardService.getCustomerRewards(
                        999L, startDate, endDate));

        verifyNoInteractions(fetchService);
    }

    @Test
    void shouldRejectReversedDateRangeBeforeFetching() {
        assertThrows(InvalidDateRangeException.class,
                () -> rewardService.getCustomerRewards(
                        101L, endDate, startDate));

        verifyNoInteractions(fetchService);
    }

    @Test
    void shouldRejectMissingStartDate() {
        assertThrows(InvalidDateRangeException.class,
                () -> rewardService.getCustomerRewards(
                        101L, null, endDate));

        verifyNoInteractions(fetchService);
    }

    @Test
    void shouldRejectMissingEndDate() {
        assertThrows(InvalidDateRangeException.class,
                () -> rewardService.getCustomerRewards(
                        101L, startDate, null));

        verifyNoInteractions(fetchService);
    }

    @Test
    void shouldRejectNonPositiveCustomerId() {
        assertThrows(IllegalArgumentException.class,
                () -> rewardService.getCustomerRewards(
                        0L, startDate, endDate));

        verifyNoInteractions(fetchService);
    }
    @Test
    void shouldKeepMonthsSeparateAcrossYears() throws Exception {
        LocalDate from = LocalDate.of(2025, 12, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        java.util.List<com.retailer.rewards.model.Transaction> purchases =
                java.util.Arrays.asList(
                        new com.retailer.rewards.model.Transaction(
                                20L, 101L, new BigDecimal("120"),
                                LocalDate.of(2025, 12, 15)),
                        new com.retailer.rewards.model.Transaction(
                                21L, 101L, new BigDecimal("75"),
                                LocalDate.of(2026, 1, 10)));

        when(fetchService.fetchTransactions(101L, from, to))
                .thenReturn(CompletableFuture.completedFuture(purchases));

        CustomerRewardResponse response =
                rewardService.getCustomerRewards(101L, from, to)
                        .get(5, TimeUnit.SECONDS);

        assertEquals(2, response.getMonthlyRewards().size());

        assertMonth(response.getMonthlyRewards().get(0),
                "2025-12", 1, "120", 90L);
        assertMonth(response.getMonthlyRewards().get(1),
                "2026-01", 1, "75", 25L);

        assertEquals(115L, response.getTotalRewardPoints());
    }

    @Test
    void shouldPropagateAsyncFetchFailure() {
        CompletableFuture<
                java.util.List<com.retailer.rewards.model.Transaction>>
                failedFetch = new CompletableFuture<>();

        IllegalStateException failure =
                new IllegalStateException("Data source unavailable");

        failedFetch.completeExceptionally(failure);

        when(fetchService.fetchTransactions(
                101L, startDate, endDate))
                .thenReturn(failedFetch);

        java.util.concurrent.ExecutionException exception =
                assertThrows(
                        java.util.concurrent.ExecutionException.class,
                        () -> rewardService.getCustomerRewards(
                                        101L, startDate, endDate)
                                .get(5, TimeUnit.SECONDS));

        assertSame(failure, exception.getCause());
    }

    @Test
    void shouldReturnPendingResponseUntilFetchCompletes()
            throws Exception {

        CompletableFuture<
                java.util.List<com.retailer.rewards.model.Transaction>>
                pendingFetch = new CompletableFuture<>();

        when(fetchService.fetchTransactions(
                101L, startDate, endDate))
                .thenReturn(pendingFetch);

        CompletableFuture<CustomerRewardResponse> responseFuture =
                assertTimeoutPreemptively(
                        java.time.Duration.ofSeconds(5),
                        () -> rewardService.getCustomerRewards(
                                101L, startDate, endDate));

        assertFalse(responseFuture.isDone());

        pendingFetch.complete(java.util.Collections.emptyList());

        CustomerRewardResponse response =
                responseFuture.get(5, TimeUnit.SECONDS);

        assertEquals(0L, response.getTotalRewardPoints());
        assertEquals(3, response.getMonthlyRewards().size());
    }

    @Test
    void shouldCalculateRewardsForAllCustomers() throws Exception {
        stubTransactions(101L, startDate, endDate);
        stubTransactions(102L, startDate, endDate);
        stubTransactions(103L, startDate, endDate);

        java.util.List<CustomerRewardResponse> responses =
                rewardService.getAllCustomerRewards(startDate, endDate)
                        .get(5, TimeUnit.SECONDS);

        assertEquals(3, responses.size());

        assertEquals(101L, responses.get(0).getCustomerId());
        assertEquals(265L, responses.get(0).getTotalRewardPoints());

        assertEquals(102L, responses.get(1).getCustomerId());
        assertEquals(212L, responses.get(1).getTotalRewardPoints());

        assertEquals(103L, responses.get(2).getCustomerId());
        assertEquals(0L, responses.get(2).getTotalRewardPoints());
        assertTrue(responses.get(2).getTransactions().isEmpty());

        verify(fetchService).fetchTransactions(
                101L, startDate, endDate);
        verify(fetchService).fetchTransactions(
                102L, startDate, endDate);
        verify(fetchService).fetchTransactions(
                103L, startDate, endDate);
    }

    @Test
    void shouldRejectInvalidRangeForAllCustomersBeforeFetching() {
        assertThrows(
                InvalidDateRangeException.class,
                () -> rewardService.getAllCustomerRewards(
                        endDate, startDate));

        verifyNoInteractions(fetchService);
    }

    @Test
    void shouldFailAllCustomerResponseWhenOneFetchFails() {
        stubTransactions(101L, startDate, endDate);
        stubTransactions(103L, startDate, endDate);

        CompletableFuture<
                java.util.List<com.retailer.rewards.model.Transaction>>
                failedFetch = new CompletableFuture<>();

        IllegalStateException failure =
                new IllegalStateException("Data source unavailable");

        failedFetch.completeExceptionally(failure);

        when(fetchService.fetchTransactions(
                102L, startDate, endDate))
                .thenReturn(failedFetch);

        java.util.concurrent.ExecutionException exception =
                assertThrows(
                        java.util.concurrent.ExecutionException.class,
                        () -> rewardService.getAllCustomerRewards(
                                        startDate, endDate)
                                .get(5, TimeUnit.SECONDS));

        assertSame(failure, exception.getCause());
    }
    private void stubTransactions(
            long customerId, LocalDate from, LocalDate to) {

        TransactionRepository repository = new TransactionRepository();

        when(fetchService.fetchTransactions(customerId, from, to))
                .thenReturn(CompletableFuture.completedFuture(
                        repository.findByCustomerIdAndDateRange(
                                customerId, from, to)));
    }

    private void assertMonth(
            MonthlyRewardSummary actual,
            String month,
            int transactionCount,
            String totalSpent,
            long points) {

        assertEquals(month, actual.getMonth());
        assertEquals(transactionCount, actual.getTransactionCount());
        assertEquals(0, new BigDecimal(totalSpent)
                .compareTo(actual.getTotalSpent()));
        assertEquals(points, actual.getRewardPoints());
    }
}