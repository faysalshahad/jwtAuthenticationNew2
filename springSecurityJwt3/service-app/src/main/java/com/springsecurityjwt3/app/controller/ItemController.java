package com.springsecurityjwt3.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springsecurityjwt3.core.dto.ItemDTO;
import com.springsecurityjwt3.core.dto.UserSummaryDTO;
import com.springsecurityjwt3.core.entity.Item;
import com.springsecurityjwt3.security.service.ItemService;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    @Autowired 
    private ItemService itemService;

    // @GetMapping
    // @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    // public List<Item> getItems() {
    //     return itemService.getAllItems();
    // }

    @GetMapping
    // @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<ItemDTO> getItems() {
        return itemService.getAllItems().stream().map(item -> {
            ItemDTO dto = new ItemDTO();
            dto.setId(item.getId());
            dto.setItemName(item.getItemName());
            dto.setItemDescription(item.getItemDescription());
            
            if (item.getCreatedBy() != null) {
                dto.setCreatedBy(new UserSummaryDTO(
                item.getCreatedBy().getId(), 
                item.getCreatedBy().getUsername()
            ));
          }
          return dto;
       }).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Item createItem(@RequestBody Item item) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return itemService.saveItem(item, username);
    }
}