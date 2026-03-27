package com.springsecurityjwt3.springSecurityJwt3.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springsecurityjwt3.springSecurityJwt3.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

}
