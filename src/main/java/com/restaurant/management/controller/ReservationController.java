
package com.restaurant.management.controller;

import com.restaurant.management.dto.ReservationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    @GetMapping("/new")
    public String showReservationForm(Model model) {
        ReservationForm form = new ReservationForm();
        form.setDate(LocalDate.now());
        form.setTimeStart(LocalTime.of(18, 0));
        form.setNumPeople(2);
        model.addAttribute("form", form);
        return "reservation/new";
    }



































//    // GET: mở trang booking, chưa có khách
//    @GetMapping
//    public String showBookingForm(Model model) {
//        BookingForm form = new BookingForm();
//        // có thể để trống hết, hoặc set mặc định
//        form.setDate(LocalDate.now());
//        form.setTimeStart(LocalTime.of(18, 0));
//        form.setNumPeople(2);
//
//        model.addAttribute("form", form);
//        model.addAttribute("timeSlots", bookingService.generateTimeSlots());
//        model.addAttribute("tables", Collections.emptyList());
//        model.addAttribute("hasCustomer", false); // chưa tìm được KH
//
//        return "booking/form";
//    }
//
//    // B1: Nhập SĐT + nhấn "Tìm khách hàng"
//    @PostMapping("/find-customer")
//    public String findCustomer(@ModelAttribute("form") BookingForm form,
//                               Model model) {
//
//        boolean hasCustomer = false;
//        if (form.getPhone() != null && !form.getPhone().isBlank()) {
//            Customer c = bookingService.findCustomerByPhone(form.getPhone());
//            if (c != null) {
//                form.setCustomerName(c.getFullName());
//                hasCustomer = true;
//            } else {
//                model.addAttribute("error", "Không tìm thấy khách hàng. Vui lòng tạo khách hàng mới.");
//            }
//        } else {
//            model.addAttribute("error", "Vui lòng nhập số điện thoại trước.");
//        }
//
//        // nếu user chưa chọn ngày/giờ thì set mặc định
//        if (form.getDate() == null) {
//            form.setDate(LocalDate.now());
//        }
//        if (form.getTimeStart() == null) {
//            form.setTimeStart(LocalTime.of(18, 0));
//        }
//        if (form.getNumPeople() == null) {
//            form.setNumPeople(2);
//        }
//
//        model.addAttribute("form", form);
//        model.addAttribute("timeSlots", bookingService.generateTimeSlots());
//        model.addAttribute("tables", Collections.emptyList());
//        model.addAttribute("hasCustomer", hasCustomer);
//
//        return "booking/form";
//    }
//
//    // B2: Sau khi đã tìm được KH -> tìm bàn trống
//    @PostMapping("/search")
//    public String searchAvailableTables(@ModelAttribute("form") BookingForm form,
//                                        Model model) {
//
//        // Bắt buộc phải có khách
//        Customer c = bookingService.findCustomerByPhone(form.getPhone());
//        if (c == null) {
//            model.addAttribute("error", "Bạn phải tìm khách hàng hợp lệ trước khi chọn bàn.");
//            model.addAttribute("hasCustomer", false);
//            model.addAttribute("timeSlots", bookingService.generateTimeSlots());
//            model.addAttribute("tables", Collections.emptyList());
//            return "booking/form";
//        }
//
//        form.setCustomerName(c.getFullName()); // hiển thị tên KH (không cho sửa)
//
//        List<RestaurantTable> tables = bookingService.findAvailableTables(form);
//
//        model.addAttribute("form", form);
//        model.addAttribute("timeSlots", bookingService.generateTimeSlots());
//        model.addAttribute("tables", tables);
//        model.addAttribute("hasCustomer", true);
//
//        return "booking/form";
//    }
//
//    @PostMapping("/confirm")
//    public String confirmBooking(@ModelAttribute("form") BookingForm form,
//                                 Model model) {
//
//        Long tableId = form.getSelectedTableId();
//
//        if (tableId == null) {
//            model.addAttribute("error", "Không xác định được bàn bạn chọn. Vui lòng thử lại.");
//            model.addAttribute("hasCustomer", true);
//            model.addAttribute("timeSlots", bookingService.generateTimeSlots());
//            model.addAttribute("tables", bookingService.findAvailableTables(form));
//            return "booking/form";
//        }
//
//        bookingService.createReservation(form, tableId);
//
//        model.addAttribute("message", "Đặt bàn thành công! Đang chờ quản lý xác nhận.");
//        return "booking/success";
//    }


}
