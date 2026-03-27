package com.springsecurityjwt3.springSecurityJwt3.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TokenRefreshRequest {

    private String refreshToken;

}
