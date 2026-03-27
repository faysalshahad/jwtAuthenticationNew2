package com.springsecurityjwt3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderLineDTO {
    private Long id;
    private Long itemId;
    private String itemName;
    private Integer quantity;
}
