package com.restaurant.management.controller;

import com.restaurant.management.entity.Booking;
import com.restaurant.management.entity.RestaurantTable;
import com.restaurant.management.entity.User;
import com.restaurant.management.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // ===============================
    // FORM CHÍNH
    // ===============================
    @GetMapping
    public String bookingForm(){
        return "booking-form";
    }

    // ===============================
    // KHÁCH CŨ
    // ===============================
    @PostMapping("/old")
    public String oldCustomer(
            @RequestParam String contact,
            Model model){

        Optional<User> user =
                bookingService.findOldCustomer(contact);

        if(user.isEmpty()){
            model.addAttribute("error","Không tìm thấy khách");
            return "booking-form";
        }

        model.addAttribute("customer", user.get());
        return "old-customer-info";
    }

    // ===============================
    // KHÁCH MỚI
    // ===============================
    @PostMapping("/new")
    public String newCustomer(
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam Integer guests,
            @RequestParam
            @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm")
            LocalDateTime time,
            Model model){

        User user =
                bookingService.createNewCustomer(name,phone,email);

        List<RestaurantTable> tables =
                bookingService.findAvailableTables(guests,time);

        model.addAttribute("tables",tables);
        model.addAttribute("customer",user);
        model.addAttribute("time",time);
        model.addAttribute("guests",guests);

        return "select-table";
    }

    // ===============================
    // XÁC NHẬN ĐẶT
    // ===============================
    @PostMapping("/confirm")
    public String confirmBooking(
            @RequestParam Integer userId,
            @RequestParam Integer tableId,
            @RequestParam Integer guests,
            @RequestParam
            @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm")
            LocalDateTime time,
            Model model){

        User user = new User();
        user.setUserId(userId);

        Booking booking =
                bookingService.createBooking(
                        user, tableId, time, guests, null);

        model.addAttribute("booking", booking);

        return "booking-success";
    }
}