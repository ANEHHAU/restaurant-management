package com.restaurant.management.repository;

import com.restaurant.management.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
     Optional<Customer> findByPhone(String phone);

     @Query("SELECT c FROM Customer c WHERE " +
             "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
             "OR c.phone LIKE CONCAT('%', :keyword, '%')")
     List<Customer> searchByKeyword(String keyword);

     boolean existsByPhone(String phone);
}
