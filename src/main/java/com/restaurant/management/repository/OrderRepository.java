package com.restaurant.management.repository;

import com.restaurant.management.model.Customer;
import com.restaurant.management.model.Order;
import com.restaurant.management.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerAndStatus(Customer customer, OrderStatus status);

    List<Order> findByCustomerId(Long customerId);


    @Modifying
    @Transactional
    void deleteByCustomerId(Long customerId);
}