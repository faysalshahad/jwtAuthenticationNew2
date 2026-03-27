package com.springsecurityjwt3.springSecurityJwt3.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springsecurityjwt3.springSecurityJwt3.entity.Item;
import com.springsecurityjwt3.springSecurityJwt3.entity.UserEntity;
import com.springsecurityjwt3.springSecurityJwt3.repository.ItemRepository;
import com.springsecurityjwt3.springSecurityJwt3.repository.UserEntityRepository;

@Service
public class ItemService {
    @Autowired 
    private ItemRepository itemRepository;
    @Autowired 
    private UserEntityRepository userRepository;

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Item saveItem(Item item, String username) {
        UserEntity admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        item.setCreatedBy(admin);
        return itemRepository.save(item);
    }
}