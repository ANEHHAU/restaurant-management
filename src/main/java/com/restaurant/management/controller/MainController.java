package com.restaurant.management.controller;

import com.restaurant.management.repository.ReservationRepository;
import com.restaurant.management.repository.TableRepository;
import com.restaurant.management.repository.CustomerRepository;
import com.restaurant.management.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

//    @todo thêm trạng thái của revelu để chuyển sang đã checkin ( có thể sau mở rộng order mang về)

    private final TableRepository tableRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;

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

        // Lấy danh sách bàn + tổng doanh thu
        List<Object[]> tables = tableRepository.findAllTableWithRevenue();

        model.addAttribute("tables", tables);
        return "table/list";
    }

    // Quản lý khách hàng: hiện full danh sách KH
//    @GetMapping("/customers")
//    public String listCustomers(Model model) {
//        model.addAttribute("customers", customerRepository.findAll());
//        return "customer/list"; // templates/customer/list.html
//    }

    // Quản lý khách hàng: hiện full danh sách KH có sl mua  giảm dần
    @GetMapping("/customers")
    public String listCustomers(Model model) {
        List<Map<String, Object>> customers = customerRepository.findAllWithCompletedOrders()
                .stream()
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", r[0]);
                    m.put("fullName", r[1]);
                    m.put("phone", r[2]);       // null OK
                    m.put("address", r[3]);     // null OK
                    m.put("createdAt", r[4]);
                    m.put("completed", r[5]);   // null OK
                    return m;
                })
                .toList();


        model.addAttribute("customers", customers);
        return "customer/list";
    }






    // Quản lý đơn hàng: hiện full danh sách đơn
    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        return "order/list"; // templates/order/list.html
    }

    // Quản lý đặt bàn: hiện full danh sách đặt bàn
    @GetMapping("/reservations")
    public String listReservations(Model model) {
        model.addAttribute("reservations", reservationRepository.findAll());
        return "reservation/list"; // templates/order/list.html
    }

}
