package com.restaurant.management.controller;


import com.restaurant.management.model.*;
import com.restaurant.management.repository.CustomerRepository;
import com.restaurant.management.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {



    @GetMapping("/new")
    public String newCustomer(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/new";
    }




}
