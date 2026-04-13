package com.springsecurityjwt3.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springsecurityjwt3.core.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

}
