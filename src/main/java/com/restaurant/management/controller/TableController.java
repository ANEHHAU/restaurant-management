package com.restaurant.management.controller;


import com.restaurant.management.model.RestaurantTable;
import com.restaurant.management.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.restaurant.management.model.Customer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/tables")
public class TableController {
    private final RestaurantTableRepository restaurantTableRepository;

    @GetMapping("/new")
    public String newTablePage(Model model) {
        model.addAttribute("tab", new Customer());
        return "table/new";
    }


    @GetMapping("/searchAvailable")
    public String searchAvailablePage(
            @RequestParam(value = "time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkTime,
            Model model) {

        // Nếu chưa chọn thời gian → mặc định là 2 tiếng
        if (checkTime == null) {
            checkTime = LocalDateTime.now().plusHours(2).withSecond(0).withNano(0);
        }


        List<RestaurantTable> availableTables = restaurantTableRepository.findAvailableTablesAt(checkTime);

        model.addAttribute("checkTime", checkTime);
        model.addAttribute("availableTables", availableTables);
        model.addAttribute("totalTables", availableTables.size());

        return "table/searchAvailable"; // → templates/table/searchAvailable.html
    }
}

