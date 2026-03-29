package com.springsecurityjwt3.springSecurityJwt3.service;

import com.springsecurityjwt3.springSecurityJwt3.entity.Customer;
import com.springsecurityjwt3.springSecurityJwt3.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> searchCustomers(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}