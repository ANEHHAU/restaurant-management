package com.restaurant.management.controller;

import com.restaurant.management.repository.RestaurantTableRepository;
import com.restaurant.management.repository.CustomerRepository;
import com.restaurant.management.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final RestaurantTableRepository tableRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    // Trang chính: menu dẫn tới 3 màn
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalTables", tableRepository.count());
        model.addAttribute("totalCustomers", customerRepository.count());
        model.addAttribute("totalOrders", orderRepository.count());
        return "index"; // src/main/resources/templates/index.html
    }

    // Quản lý bàn: hiện full danh sách bàn
    @GetMapping("/tables")
    public String listTables(Model model) {
        model.addAttribute("tables", tableRepository.findAll());
        return "table/list"; // templates/table/list.html
    }

    // Quản lý khách hàng: hiện full danh sách KH
    @GetMapping("/customers")
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        return "customer/list"; // templates/customer/list.html
    }

    // Quản lý đơn hàng: hiện full danh sách đơn
    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        return "order/list"; // templates/order/list.html
    }
}
