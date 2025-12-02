// src/main/java/com/restaurant/management/controller/web/DishController.java
package com.restaurant.management.controller;

import com.restaurant.management.model.Dish;
import com.restaurant.management.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dishes")
public class DishController {

    @Autowired
    private DishRepository dishRepository;

    @GetMapping
    public String listDishes(Model model) {

        model.addAttribute("dishes", dishRepository.findAllOrderByCreatedAtDesc());

        List<Object[]> result = dishRepository.findMostPopularDish();

        if (!result.isEmpty()) {
            Object[] row = result.get(0);

            Dish popularDish = (Dish) row[0];
            Long totalQty = (Long) row[1];

            model.addAttribute("popularDish", popularDish);
            model.addAttribute("popularCount", totalQty);
        } else {
            model.addAttribute("popularDish", null);
        }

        return "dish/list";
    }

    @GetMapping("/new")
    public String addDishPage() {
        return "dish/new";
    }

}