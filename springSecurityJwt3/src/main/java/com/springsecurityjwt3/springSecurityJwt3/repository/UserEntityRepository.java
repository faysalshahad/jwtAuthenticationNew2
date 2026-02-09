package com.springsecurityjwt3.springSecurityJwt3.repository;

import com.springsecurityjwt3.springSecurityJwt3.entity.UserEntity;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

}
