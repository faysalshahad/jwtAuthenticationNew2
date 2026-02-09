package com.springsecurityjwt3.springSecurityJwt3.service;

import com.springsecurityjwt3.springSecurityJwt3.entity.UserEntity;
import com.springsecurityjwt3.springSecurityJwt3.repository.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserEntityService implements UserDetailsService {

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userEntityRepository.findByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("User Not Found: " + username));

        return User
                .withUsername(userEntity.getUsername())
                .password(userEntity.getPassword())
                .accountLocked(!userEntity.isAccountNonLocked())
                .authorities("ROLE_" + userEntity.getRole())
                .build();
    }

    public void increaseFailedAttempts(UserEntity userEntity) {
        int newFailAttempts = userEntity.getFailedAttempt() + 1;
        userEntity.setFailedAttempt(newFailAttempts);
        if(newFailAttempts >= 5){
            userEntity.setAccountNonLocked(false);
            userEntity.setLockTime(LocalDateTime.now());
        }
        userEntityRepository.save(userEntity);
    }

    public void resetFailedAttempts(String username){
        userEntityRepository.findByUsername(username).ifPresent(userEntityParam ->{
            userEntityParam.setFailedAttempt(0);
            userEntityRepository.save(userEntityParam);
        });
    }
}
