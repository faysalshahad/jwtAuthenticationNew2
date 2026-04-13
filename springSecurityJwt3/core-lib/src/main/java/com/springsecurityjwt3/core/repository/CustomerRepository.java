package com.springsecurityjwt3.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springsecurityjwt3.core.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Search by name (case-insensitive)
    List<Customer> findByNameContainingIgnoreCase(String name);
}