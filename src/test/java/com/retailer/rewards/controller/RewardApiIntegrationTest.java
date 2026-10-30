package com.retailer.rewards.controller;

import com.retailer.rewards.RewardsApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = RewardsApplication.class)
@AutoConfigureMockMvc
class RewardApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnCustomerRewards() throws Exception {
        MvcResult pending = mockMvc.perform(
                        get("/api/customers/101/rewards")
                                .param("startDate", "2026-07-01")
                                .param("endDate", "2026-09-30"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "application/json"))
                .andExpect(jsonPath("$.customerId").value(101))
                .andExpect(jsonPath("$.customerName")
                        .value("Alice Morgan"))
                .andExpect(jsonPath("$.startDate")
                        .value("2026-07-01"))
                .andExpect(jsonPath("$.endDate")
                        .value("2026-09-30"))
                .andExpect(jsonPath("$.monthlyRewards", hasSize(3)))
                .andExpect(jsonPath("$.transactions", hasSize(6)))
                .andExpect(jsonPath("$.totalSpent").value(525.75))
                .andExpect(jsonPath("$.totalRewardPoints").value(265))
                .andExpect(jsonPath("$.monthlyRewards[0].rewardPoints")
                        .value(115))
                .andExpect(jsonPath("$.transactions[0].date")
                        .value("2026-07-01"))
                .andExpect(jsonPath("$.transactions[0].rewardPoints")
                        .value(90));
    }

    @Test
    void shouldReturnRewardsForAllCustomers() throws Exception {
        MvcResult pending = mockMvc.perform(
                        get("/api/rewards")
                                .param("startDate", "2026-07-01")
                                .param("endDate", "2026-09-30"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].totalRewardPoints").value(265))
                .andExpect(jsonPath("$[1].totalRewardPoints").value(212))
                .andExpect(jsonPath("$[2].totalRewardPoints").value(0));
    }

    @Test
    void shouldReturnZeroForCustomerWithoutTransactions()
            throws Exception {

        MvcResult pending = mockMvc.perform(
                        get("/api/customers/103/rewards")
                                .param("startDate", "2026-07-01")
                                .param("endDate", "2026-09-30"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(103))
                .andExpect(jsonPath("$.transactions", hasSize(0)))
                .andExpect(jsonPath("$.monthlyRewards", hasSize(3)))
                .andExpect(jsonPath("$.totalRewardPoints").value(0));
    }

    @Test
    void shouldReturnNotFoundForUnknownCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/999/rewards")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-09-30"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Customer not found with ID: 999"))
                .andExpect(jsonPath("$.path")
                        .value("/api/customers/999/rewards"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldRejectReversedDateRange() throws Exception {
        mockMvc.perform(get("/api/customers/101/rewards")
                        .param("startDate", "2026-09-30")
                        .param("endDate", "2026-07-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldRejectMissingRequiredParameter() throws Exception {
        mockMvc.perform(get("/api/rewards")
                        .param("startDate", "2026-07-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required parameter is missing: endDate"));
    }

    @Test
    void shouldRejectInvalidCalendarDate() throws Exception {
        mockMvc.perform(get("/api/rewards")
                        .param("startDate", "2026-02-30")
                        .param("endDate", "2026-09-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldRejectNonNumericCustomerId() throws Exception {
        mockMvc.perform(get("/api/customers/abc/rewards")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-09-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldRejectNonPositiveCustomerId() throws Exception {
        mockMvc.perform(get("/api/customers/0/rewards")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-09-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Customer ID must be positive"));
    }
}