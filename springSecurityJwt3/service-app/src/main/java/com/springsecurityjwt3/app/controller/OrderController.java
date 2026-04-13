package com.springsecurityjwt3.app.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springsecurityjwt3.core.entity.OrderHeader;
import com.springsecurityjwt3.core.entity.OrderLine;
import com.springsecurityjwt3.security.service.OrderService;

@RestController
@RequestMapping("/auth/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderHeader> createHeader(@RequestBody OrderHeader header) {
        header.setOrderDate(LocalDateTime.now());
        return ResponseEntity.ok(orderService.saveHeader(header));
    }

    @GetMapping
    public List<OrderHeader> getAllOrders() {
        return orderService.findAll();
    }

    @PostMapping("/{headerId}/lines")
    public ResponseEntity<OrderLine> addLine(@PathVariable Long headerId, @RequestBody OrderLine line) {
        return ResponseEntity.ok(orderService.saveLine(headerId, line));
    }

    // Update Header
    @PutMapping("/{id}")
    public OrderHeader updateHeader(@PathVariable Long id, @RequestBody OrderHeader header) {
        return orderService.updateHeader(id, header);
    }

    // Delete Header
    @DeleteMapping("/{id}")
    public void deleteHeader(@PathVariable Long id) {
        orderService.deleteHeader(id);
    }

    // Update Line
    @PutMapping("/lines/{lineId}")
    public OrderLine updateLine(@PathVariable Long lineId, @RequestBody OrderLine line) {
        return orderService.updateLine(lineId, line);
    }

    // Delete Line
    @DeleteMapping("/lines/{lineId}")
    public void deleteLine(@PathVariable Long lineId) {
        orderService.deleteLine(lineId);
    }

    
}
