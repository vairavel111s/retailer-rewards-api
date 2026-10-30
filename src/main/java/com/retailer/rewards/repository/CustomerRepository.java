package com.retailer.rewards.repository;

import com.retailer.rewards.model.Customer;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepository {

    private final List<Customer> customers =
            Collections.unmodifiableList(Arrays.asList(
                    new Customer(101L, "Alice Morgan"),
                    new Customer(102L, "Brian Wilson"),
                    new Customer(103L, "Clara Evans")
            ));

    public Optional<Customer> findById(long customerId) {
        return customers.stream()
                .filter(customer -> customer.getId() == customerId)
                .findFirst();
    }

    public List<Customer> findAll() {
        return customers;
    }
}