package com.retailer.rewards.repository;

import com.retailer.rewards.model.Customer;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerRepositoryTest {

    private final CustomerRepository repository =
            new CustomerRepository();

    @Test
    void shouldFindExistingCustomer() {
        Optional<Customer> result = repository.findById(101L);

        assertTrue(result.isPresent());
        assertEquals(101L, result.get().getId());
        assertEquals("Alice Morgan", result.get().getName());
    }

    @Test
    void shouldReturnEmptyForUnknownCustomer() {
        assertFalse(repository.findById(999L).isPresent());
    }

    @Test
    void shouldIncludeCustomerWithoutTransactions() {
        Optional<Customer> result = repository.findById(103L);

        assertTrue(result.isPresent());
        assertEquals("Clara Evans", result.get().getName());
    }

    @Test
    void shouldPreventModificationOfCustomerDataset() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> repository.findAll().clear());

        assertEquals(3, repository.findAll().size());
    }
}