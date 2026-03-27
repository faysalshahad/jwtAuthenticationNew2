package com.springsecurityjwt3.springSecurityJwt3.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springsecurityjwt3.springSecurityJwt3.entity.OrderLine;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

}
