package com.springsecurityjwt3.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springsecurityjwt3.core.entity.OrderLine;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

}
