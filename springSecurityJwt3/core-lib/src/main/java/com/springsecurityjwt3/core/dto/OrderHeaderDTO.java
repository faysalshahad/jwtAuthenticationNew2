package com.springsecurityjwt3.core.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderHeaderDTO {
    private Long id;
    private LocalDateTime orderDate;
    private Long userId;
    private String username;
    private List<OrderLineDTO> lines;
}


