package com.springsecurityjwt3.springSecurityJwt3.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springsecurityjwt3.springSecurityJwt3.entity.Customer;
import com.springsecurityjwt3.springSecurityJwt3.entity.Item;
import com.springsecurityjwt3.springSecurityJwt3.entity.OrderHeader;
import com.springsecurityjwt3.springSecurityJwt3.entity.OrderLine;
import com.springsecurityjwt3.springSecurityJwt3.entity.UserEntity;
import com.springsecurityjwt3.springSecurityJwt3.repository.CustomerRepository;
import com.springsecurityjwt3.springSecurityJwt3.repository.ItemRepository;
import com.springsecurityjwt3.springSecurityJwt3.repository.OrderHeaderRepository;
import com.springsecurityjwt3.springSecurityJwt3.repository.OrderLineRepository;
import com.springsecurityjwt3.springSecurityJwt3.repository.UserEntityRepository;

@Service
public class OrderService {

    @Autowired 
    private OrderHeaderRepository headerRepository;
    @Autowired 
    private OrderLineRepository lineRepository;
    @Autowired 
    private CustomerRepository customerRepository;
    @Autowired 
    private ItemRepository itemRepository;

    // --- ORDER Header METHODS ---

    public List<OrderHeader> findAll() {
        return headerRepository.findAll();
    }

    // public OrderHeader saveHeader(OrderHeader header) {
    //     // Ensure the user exists in DB before linking
    //     if (header.getUser() != null && header.getUser().getId() != null) {
    //         UserEntity user = userRepository.findById(header.getUser().getId())
    //                 .orElseThrow(() -> new RuntimeException("User not found"));
    //         header.setUser(user);
    //     }
    //     return headerRepository.save(header);
    // }

    public void deleteHeader(Long id) {
        // CascadeType.ALL in the Entity handles the deletion of lines automatically
        headerRepository.deleteById(id);
    }

    // public OrderHeader updateHeader(Long id, OrderHeader updatedHeader) {
    //     OrderHeader existing = headerRepository.findById(id)
    //             .orElseThrow(() -> new RuntimeException("Order not found"));
        
    //     // Update user if changed
    //     if (updatedHeader.getUser() != null) {
    //         UserEntity user = userRepository.findById(updatedHeader.getUser().getId()).orElse(null);
    //         existing.setUser(user);
    //     }
    //     return headerRepository.save(existing);
    // }

    public OrderHeader saveHeader(OrderHeader header) {
        if (header.getCustomer() != null && header.getCustomer().getId() != null) {
            Customer customer = customerRepository.findById(header.getCustomer().getId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            header.setCustomer(customer);
        }
        return headerRepository.save(header);
    }

    public OrderHeader updateHeader(Long id, OrderHeader updatedHeader) {
        OrderHeader existing = headerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (updatedHeader.getCustomer() != null) {
            Customer customer = customerRepository.findById(updatedHeader.getCustomer().getId()).orElse(null);
            existing.setCustomer(customer);
        }
        return headerRepository.save(existing);
    }

    // --- ORDER LINE METHODS ---

    public OrderLine saveLine(Long headerId, OrderLine line) {
        OrderHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new RuntimeException("Header not found"));
        
        // Link Line to Header
        line.setOrderHeader(header);
        
        // Ensure Item exists
        if (line.getItem() != null && line.getItem().getId() != null) {
            Item item = itemRepository.findById(line.getItem().getId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));
            line.setItem(item);
        }
        
        return lineRepository.save(line);
    }

    public void deleteLine(Long lineId) {
        lineRepository.deleteById(lineId);
    }

    public OrderLine updateLine(Long lineId, OrderLine updatedLine) {
        OrderLine existing = lineRepository.findById(lineId)
                .orElseThrow(() -> new RuntimeException("Line not found"));
        
        existing.setQuantity(updatedLine.getQuantity());
        // You can also update the Item here if needed
        return lineRepository.save(existing);
    }
}
