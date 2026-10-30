package com.retailer.rewards.model;

public final class Customer {

    private final long id;
    private final String name;

    public Customer(long id, String name) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Customer ID must be positive");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Customer name must not be blank");
        }

        this.id = id;
        this.name = name.trim();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}