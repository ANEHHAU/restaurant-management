package com.restaurant.management.controller;


import com.restaurant.management.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/tables")
public class TableController {


    @GetMapping("/new")
    public String newTablePage(Model model) {
        model.addAttribute("tab", new Customer());
        return "table/new";
    }
}
