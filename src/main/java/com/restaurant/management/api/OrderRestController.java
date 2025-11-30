package com.restaurant.management.api;

import com.restaurant.management.model.Order;
import com.restaurant.management.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderRepository orderRepository;

    // GET /orders/list
    @GetMapping("/list")
    public List<Order> listOrders() {
        return orderRepository.findAll();
    }
}
