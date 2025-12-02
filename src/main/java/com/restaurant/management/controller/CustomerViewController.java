//package com.restaurant.management.controller;
//
//import com.restaurant.management.model.Order;
//import com.restaurant.management.model.RestaurantTable;
//import com.restaurant.management.repository.OrderRepository;
//import com.restaurant.management.repository.RestaurantTableRepository;
//import com.restaurant.management.repository.DishRepository;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@Controller
//@RequestMapping("/customer/order")
//public class CustomerViewController {
//    @Autowired
//    private RestaurantTableRepository tableRepository;
//
//    @Autowired
//    private DishRepository dishRepository;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//
//    @GetMapping("/order/table/{tableId}")
//    public String openCustomerOrder(@PathVariable Long tableId, Model model) {
//
//        RestaurantTable table = tableRepository.findById(tableId).orElse(null);
//
//        if (table == null) {
//            model.addAttribute("error", "Không tìm thấy bàn!");
//            return "customerView/404";
//        }
//
//        // 1️⃣ Tìm order đang hoạt động của bàn
//        Optional<Order> activeOrder = orderRepository.findActiveOrderByTable(tableId);
//
//        if (activeOrder.isEmpty()) {
//            // 2️⃣ Chưa có order → bắt khách nhập thông tin
//            model.addAttribute("table", table);
//            return "customerView/customerInfor";  // giao diện khách tự tạo order
//        }
//
//        // 3️⃣ Đã có order → vào menu gọi món
//        model.addAttribute("order", activeOrder);
//        model.addAttribute("table", table);
//        model.addAttribute("dishes", dishRepository.findAll());
//
//        return "customerView/orderMenu";
//    }
//
//
//}
//
package com.restaurant.management.controller;

import com.restaurant.management.model.*;
import com.restaurant.management.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerViewController {

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CustomerRepository customerRepository; // Thêm repository này!





    @GetMapping("/homePage")
    public String homePage() {
        return "customerView/homePage";  // resources/templates/customerView/homePage.html
    }

    // Trang quét QR để gọi món
    // URL: /customer/scanQrPage → trả về view customerView/scanQrPage
    @GetMapping("/scanQrPage")
    public String scanQrPage() {
        return "customerView/scanQrPage";
    }

    // Trang đặt bàn
    // URL: /customer/booking → trả về view customerView/book
    @GetMapping("/booking")
    public String booking() {
        return "customerView/book";
    }

    // Trang xem hóa đơn
    // URL: /customer/invoice → trả về view customerView/invoice
    @GetMapping("/invoice")
    public String invoice() {
//        sau thêm id cho từng khách hàng để tìm
        return "customerView/invoice";
    }


































//    -------------------------------------------------


    @GetMapping("/order/table/{tableId}")
    public String openCustomerOrder(@PathVariable Long tableId, Model model) {

        RestaurantTable table = tableRepository.findById(tableId)
                .orElse(null);

        if (table == null) {
            model.addAttribute("error", "Không tìm thấy bàn!");
            return "customerView/404";
        }


        Optional<Order> activeOrderOpt = orderRepository.findActiveOrderByTable(tableId);

        if (activeOrderOpt.isEmpty()) {
            // Chưa có order → yêu cầu khách nhập thông tin
            model.addAttribute("table", table);
            return "customerView/customerInform";
        }

        Order activeOrder = activeOrderOpt.get();

        model.addAttribute("order", activeOrder);
        model.addAttribute("table", table);
        model.addAttribute("customer", activeOrder.getCustomer()); // để hiển thị tên, sđt
        model.addAttribute("dishes", dishRepository.findAll());

        return "customerView/orderMenu";
    }

    /**
     * Tạo order mới: khách tự nhập tên + sđt + số lượng người
     */
    @PostMapping("/order/create")
    public String createCustomerOrder(
            @RequestParam Long tableId,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam Integer guestCount,
            Model model) {

        RestaurantTable table = tableRepository.findById(tableId)
                .orElse(null);
        if (table == null) {
            return "customerView/404";
        }

        // Tìm khách hàng theo số điện thoại (tránh tạo trùng)
        Customer customer = customerRepository.findByPhone(phone)
                .orElseGet(() -> {
                    // Nếu chưa có → tạo mới
                    Customer newCustomer = Customer.builder()
                            .fullName(fullName)
                            .phone(phone)
                            .build();
                    return customerRepository.save(newCustomer);
                });

        // Cập nhật tên nếu khác (trong trường hợp khách cũ đổi tên)
        if (!customer.getFullName().equals(fullName)) {
            customer.setFullName(fullName);
            customerRepository.save(customer);
        }

        // Tạo order mới
        Order order = Order.builder()
                .table(table)
                .customer(customer)
                .guestCount(guestCount)
                .status(OrderStatus.NEW)
                .orderTime(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        return "redirect:/customer/order/table/" + tableId;
    }

    /**
     * Tìm order cũ bằng số điện thoại (dành cho khách quay lại bàn đã có order)
     */
    @PostMapping("/order/find-by-phone")
    public String findOrderByPhone(
            @RequestParam Long tableId,
            @RequestParam String phone,
            Model model) {

        Optional<Customer> customerOpt = customerRepository.findByPhone(phone);

        if (customerOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy khách hàng với số điện thoại này!");
            RestaurantTable table = tableRepository.findById(tableId).orElse(null);
            model.addAttribute("table", table);
            return "customerView/customerInform";
        }

        Customer customer = customerOpt.get();

        // Tìm order đang active của khách tại bàn này
        Optional<Order> activeOrder = orderRepository.findActiveOrderByCustomerAndTable(customer.getId(), tableId);

        if (activeOrder.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy đơn hàng đang hoạt động với số điện thoại này tại bàn!");
            RestaurantTable table = tableRepository.findById(tableId).orElse(null);
            model.addAttribute("table", table);
            return "customerView/customerInform";
        }

        return "redirect:/customer/order/table/" + tableId;
    }




    @PostMapping("/order/add-items")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addOrderItems(
            @RequestBody Map<String, Object> payload,
            HttpSession session) { // hoặc dùng session để chống gọi bậy (tùy chọn)

        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Lấy orderId từ payload
            Object orderIdObj = payload.get("orderId");
            if (orderIdObj == null) {
                response.put("success", false);
                response.put("message", "Thiếu thông tin đơn hàng");
                return ResponseEntity.badRequest().body(response);
            }

            Long orderId = Long.valueOf(orderIdObj.toString());

            // 2. Tìm order + kiểm tra hợp lệ
            Order order = orderRepository.findById(orderId)
                    .orElse(null);

            if (order == null) {
                response.put("success", false);
                response.put("message", "Đơn hàng không tồn tại");
                return ResponseEntity.badRequest().body(response);
            }

            // Kiểm tra trạng thái: chỉ cho thêm món khi còn NEW hoặc IN_PROGRESS
            if (!(order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.IN_PROGRESS)) {
                response.put("success", false);
                response.put("message", "Đơn hàng đã hoàn tất, không thể thêm món!");
                return ResponseEntity.badRequest().body(response);
            }

            // 3. Lấy danh sách món từ payload
            Object itemsObj = payload.get("items");
            if (!(itemsObj instanceof List)) {
                response.put("success", false);
                response.put("message", "Danh sách món không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            List<?> rawItems = (List<?>) itemsObj;
            if (rawItems.isEmpty()) {
                response.put("success", false);
                response.put("message", "Chưa chọn món nào");
                return ResponseEntity.badRequest().body(response);
            }

            BigDecimal totalAdded = BigDecimal.ZERO;
            int count = 0;

            for (Object itemObj : rawItems) {
                if (!(itemObj instanceof Map)) continue;
                Map<?, ?> item = (Map<?, ?>) itemObj;

                Long dishId = Long.valueOf(item.get("dishId").toString());
                Integer quantity = Integer.valueOf(item.get("quantity").toString());

                if (quantity == null || quantity <= 0) continue;

                Dish dish = dishRepository.findById(dishId)
                        .orElse(null);

                if (dish == null || !dish.isActive()) {
                    continue; // bỏ qua món không tồn tại hoặc đã ẩn
                }

                // Kiểm tra xem đã có món này trong order chưa → cập nhật hoặc tạo mới
                Optional<OrderDetail> existingDetail = order.getOrderDetails().stream()
                        .filter(od -> od.getDish().getId().equals(dishId))
                        .findFirst();

                if (existingDetail.isPresent()) {
                    OrderDetail detail = existingDetail.get();
                    detail.setQuantity(detail.getQuantity() + quantity);
                    detail.setUnitPrice(dish.getUnitPrice());
                    orderDetailRepository.save(detail);
                } else {
                    OrderDetail newDetail = OrderDetail.builder()
                            .order(order)
                            .dish(dish)
                            .quantity(quantity)
                            .unitPrice(dish.getUnitPrice())
                            .build();
                    orderDetailRepository.save(newDetail);
                    order.getOrderDetails().add(newDetail);
                }

                totalAdded = totalAdded.add(dish.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
                count++;
            }

            // Cập nhật trạng thái đơn hàng nếu cần
            if (order.getStatus() == OrderStatus.NEW) {
                order.setStatus(OrderStatus.IN_PROGRESS);
                orderRepository.save(order);
            }

            response.put("success", true);
            response.put("message", "Đã thêm " + count + " món vào đơn hàng!");
            response.put("totalAdded", totalAdded);
            response.put("newTotal", calculateOrderTotal(order)); // hàm tự viết dưới

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra, vui lòng thử lại!");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Hàm phụ trợ tính tổng tiền đơn hàng
    private BigDecimal calculateOrderTotal(Order order) {
        return order.getOrderDetails().stream()
                .map(OrderDetail::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}