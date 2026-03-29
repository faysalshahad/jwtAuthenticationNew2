package com.springsecurityjwt3.springSecurityJwt3.repository;

import com.springsecurityjwt3.springSecurityJwt3.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Search by name (case-insensitive)
    List<Customer> findByNameContainingIgnoreCase(String name);
}