package com.retailer.rewards.controller;

import com.retailer.rewards.dto.CustomerRewardResponse;
import com.retailer.rewards.service.RewardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping("/customers/{customerId}/rewards")
    public CompletableFuture<CustomerRewardResponse> getCustomerRewards(
            @PathVariable("customerId") long customerId,
            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return rewardService.getCustomerRewards(
                customerId, startDate, endDate);
    }

    @GetMapping("/rewards")
    public CompletableFuture<List<CustomerRewardResponse>>
    getAllCustomerRewards(
            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return rewardService.getAllCustomerRewards(startDate, endDate);
    }
}