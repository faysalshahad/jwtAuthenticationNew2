package com.springsecurityjwt3.springSecurityJwt3.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

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
@JsonInclude(JsonInclude.Include.NON_NULL) // Hides the token field if it's null
public class AuthResponse {

    private String message;
    private String role;
    private String accessToken;
    private String refreshToken;

    // Helper constructor for Cookie-based login (no tokens in body)
    public AuthResponse(String message, String role) {
        this.message = message;
        this.role = role;
    }

//     // Customized Constructor
//     public AuthResponse(String message, String role) {
//         this.message = message;
//         this.role = role;
//         this.accessToken = null;
//         this.refreshToken = null;
// }
    

}
