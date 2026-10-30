package com.retailer.rewards.service;

import com.retailer.rewards.dto.CustomerRewardResponse;
import com.retailer.rewards.dto.MonthlyRewardSummary;
import com.retailer.rewards.dto.TransactionDetail;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.InvalidDateRangeException;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class RewardService {

    private final CustomerRepository customerRepository;
    private final TransactionFetchService transactionFetchService;
    private final RewardCalculator rewardCalculator;

    public RewardService(
            CustomerRepository customerRepository,
            TransactionFetchService transactionFetchService,
            RewardCalculator rewardCalculator) {

        this.customerRepository = customerRepository;
        this.transactionFetchService = transactionFetchService;
        this.rewardCalculator = rewardCalculator;
    }

    public CompletableFuture<CustomerRewardResponse> getCustomerRewards(
            long customerId, LocalDate startDate, LocalDate endDate) {

        validateDateRange(startDate, endDate);

        if (customerId <= 0) {
            throw new IllegalArgumentException(
                    "Customer ID must be positive");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(
                        () -> new CustomerNotFoundException(customerId));

        return transactionFetchService.fetchTransactions(
                        customerId, startDate, endDate)
                .thenApply(transactions ->
                        buildResponse(
                                customer, startDate, endDate, transactions));
    }
    public CompletableFuture<List<CustomerRewardResponse>> getAllCustomerRewards(
            LocalDate startDate, LocalDate endDate) {

        validateDateRange(startDate, endDate);

        List<CompletableFuture<CustomerRewardResponse>> pendingRewards =
                customerRepository.findAll().stream()
                        .map(customer -> getCustomerRewards(
                                customer.getId(), startDate, endDate))
                        .collect(Collectors.toList());

        CompletableFuture<?>[] futures =
                pendingRewards.toArray(new CompletableFuture<?>[0]);

        return CompletableFuture.allOf(futures)
                .thenApply(ignored -> pendingRewards.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    private void validateDateRange(
            LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new InvalidDateRangeException(
                    "Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(
                    "Start date must not be after end date");
        }
    }

    private CustomerRewardResponse buildResponse(
            Customer customer,
            LocalDate startDate,
            LocalDate endDate,
            List<Transaction> transactions) {

        List<TransactionDetail> details = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate)
                        .thenComparingLong(Transaction::getId))
                .map(transaction -> new TransactionDetail(
                        transaction.getId(),
                        transaction.getDate(),
                        transaction.getAmount(),
                        rewardCalculator.calculatePoints(
                                transaction.getAmount())))
                .collect(Collectors.toList());

        Map<YearMonth, List<TransactionDetail>> transactionsByMonth =
                details.stream().collect(Collectors.groupingBy(
                        detail -> YearMonth.from(detail.getDate())));

        List<MonthlyRewardSummary> monthlyRewards = new ArrayList<>();
        BigDecimal totalSpent = BigDecimal.ZERO;
        long totalRewardPoints = 0L;

        YearMonth month = YearMonth.from(startDate);
        YearMonth lastMonth = YearMonth.from(endDate);

        while (true) {
            List<TransactionDetail> monthTransactions =
                    transactionsByMonth.getOrDefault(
                            month, java.util.Collections.emptyList());

            BigDecimal monthlySpent = BigDecimal.ZERO;
            long monthlyPoints = 0L;

            for (TransactionDetail detail : monthTransactions) {
                monthlySpent = monthlySpent.add(detail.getAmount());
                monthlyPoints = Math.addExact(
                        monthlyPoints, detail.getRewardPoints());
            }

            monthlyRewards.add(new MonthlyRewardSummary(
                    month.toString(),
                    monthTransactions.size(),
                    monthlySpent,
                    monthlyPoints));

            totalSpent = totalSpent.add(monthlySpent);
            totalRewardPoints = Math.addExact(
                    totalRewardPoints, monthlyPoints);

            if (month.equals(lastMonth)) {
                break;
            }

            month = month.plusMonths(1);
        }

        return new CustomerRewardResponse(
                customer.getId(),
                customer.getName(),
                startDate,
                endDate,
                monthlyRewards,
                details,
                totalSpent,
                totalRewardPoints);
    }
}