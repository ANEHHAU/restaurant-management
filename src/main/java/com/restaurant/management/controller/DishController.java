// src/main/java/com/restaurant/management/controller/web/DishController.java
package com.restaurant.management.controller;

import com.restaurant.management.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dishes")
public class DishController {

    @Autowired
    private DishRepository dishRepository;

    @GetMapping
    public String listDishes(Model model) {
        model.addAttribute("dishes", dishRepository.findAllOrderByCreatedAtDesc());
        return "dish/list"; // → templates/dish/list.html
    }

    @GetMapping("/new")
    public String addDishPage() {
        return "dish/new";
    }

}