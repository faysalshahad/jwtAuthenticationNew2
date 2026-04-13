package com.springsecurityjwt3.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springsecurityjwt3.core.entity.OrderHeader;

public interface OrderHeaderRepository extends JpaRepository<OrderHeader, Long> {

}
