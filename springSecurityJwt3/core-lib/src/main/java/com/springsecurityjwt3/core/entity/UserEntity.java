package com.springsecurityjwt3.core.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String role;

    // Account Lockout Features
    private boolean accountNonLocked = true;
    private int failedAttempt = 0;
    private LocalDateTime lockTime;

    private LocalDateTime lastLogoutDate;

    //Refresh Token Features
    private String refreshToken;
    private LocalDateTime refreshTokenExpiry;

    //Item
    // @OneToMany(mappedBy = "createdBy")
    // @JsonIgnore
    // private List<Item> items;

}