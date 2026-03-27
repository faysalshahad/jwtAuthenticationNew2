package com.springsecurityjwt3.springSecurityJwt3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ItemDTO {
    private Long id;
    private String itemName;
    private String itemDescription;
    private UserSummaryDTO createdBy; // Only id and username
}