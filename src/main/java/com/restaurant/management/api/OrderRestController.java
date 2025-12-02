package com.restaurant.management.api;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.restaurant.management.model.*;
import com.restaurant.management.repository.DishRepository;
import com.restaurant.management.repository.OrderRepository;
import com.restaurant.management.repository.TableRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderRestController {

        @Autowired
        private OrderRepository orderRepository;
        @Autowired
        private TableRepository tableRepository;
        @Autowired
        private DishRepository dishRepository;


    @GetMapping("/{orderId}/items")
    public Map<String, Object> getOrderItems(@PathVariable Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Map danh sách món
        List<Map<String, Object>> items = order.getOrderDetails().stream()
                .map(od -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("dishName", od.getDish().getDishName());
                    m.put("quantity", od.getQuantity());
                    m.put("unitPrice", od.getUnitPrice());
                    m.put("lineTotal", od.getLineTotal());
                    return m;
                })
                .toList();

        // Map tổng response
        Map<String, Object> response = new HashMap<>();
        response.put("customerName",
                order.getCustomer() != null ? order.getCustomer().getFullName() : "Khách vãng lai");

        response.put("tableName",
                order.getTable() != null ? order.getTable().getTableName() : "—");

        response.put("items", items);

        return response;
    }










    @PostMapping("/api/orders")
    @Transactional
    public ResponseEntity<String> createOrder(@RequestBody Map<String, Object> data) {

        Long tableId = Long.valueOf(data.get("tableId").toString());
        Integer guestCount = data.get("guestCount") != null ? Integer.valueOf(data.get("guestCount").toString()) : null;
        String note = data.get("note") != null ? data.get("note").toString().trim() : null;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> detailsJson = (List<Map<String, Object>>) data.get("orderDetails");

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bàn"));

        if (table.getStatus() != TableStatus.INACTIVE) {
            return ResponseEntity.badRequest().body("Bàn đang được sử dụng!");
        }

        // --- TRẠNG THÁI ĐƠN HÀNG ---
        OrderStatus orderStatus = (detailsJson != null && !detailsJson.isEmpty())
                ? OrderStatus.IN_PROGRESS   // Có món -> bắt đầu chế biến
                : OrderStatus.NEW;          // Không có món (hầu như không xảy ra)

        Order order = Order.builder()
                .table(table)
                .orderTime(LocalDateTime.now())
                .status(orderStatus)
                .guestCount(guestCount)
                .note(note)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<OrderDetail> details = new ArrayList<>();
        for (Map<String, Object> item : detailsJson) {
            Long dishId = Long.valueOf(item.get("dishId").toString());
            int qty = Integer.parseInt(item.get("quantity").toString());

            Dish dish = dishRepository.findById(dishId)
                    .orElseThrow(() -> new IllegalArgumentException("Món không tồn tại: " + dishId));

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .dish(dish)
                    .quantity(qty)
                    .unitPrice(dish.getUnitPrice())
                    .build();

            details.add(detail);
        }

        order.setOrderDetails(details);

        // --- CẬP NHẬT TRẠNG THÁI BÀN ---
        table.setStatus(TableStatus.ACTIVE);
        tableRepository.save(table);

        // Lưu order
        orderRepository.save(order);

        return ResponseEntity.ok("Tạo đơn hàng thành công! Mã đơn: " + order.getId());
    }


    // GET /orders/list
    @GetMapping("/list")
    public List<Order> listOrders() {
        return orderRepository.findAll();
    }
}
