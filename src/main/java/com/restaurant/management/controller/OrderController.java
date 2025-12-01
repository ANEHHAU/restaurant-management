package com.restaurant.management.controller;


import com.restaurant.management.model.Customer;
import com.restaurant.management.model.Order;
import com.restaurant.management.repository.DishRepository;
import com.restaurant.management.repository.OrderRepository;
import com.restaurant.management.repository.ReservationRepository;
import com.restaurant.management.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {


    @Autowired
    private ReservationRepository reservationRepository;

@Autowired
private OrderRepository orderRepository;
@Autowired
private TableRepository tableRepository;
@Autowired
private DishRepository dishRepository;


@GetMapping("/new")
    public String newOrderPage(Model model) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime start = now.toLocalDate().atStartOfDay();
    LocalDateTime end = now.toLocalDate().atTime(23, 59, 59);

    model.addAttribute("tables", tableRepository.findAllInActiveTables());
    model.addAttribute("dishes", dishRepository.findAll());
    model.addAttribute("reservations", reservationRepository.findActiveReservationsForToday(start,end));
        return "order/new";
    }


}