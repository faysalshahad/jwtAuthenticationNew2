package com.springsecurityjwt3.springSecurityJwt3.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springsecurityjwt3.springSecurityJwt3.entity.OrderHeader;

public interface OrderHeaderRepository extends JpaRepository<OrderHeader, Long> {

}
