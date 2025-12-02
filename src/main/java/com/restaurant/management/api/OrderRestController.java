package com.restaurant.management.api;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.restaurant.management.model.*;
import com.restaurant.management.repository.CustomerRepository;
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

    @Autowired
    private CustomerRepository customerRepository;


    @PutMapping("/{orderId}/update")
    @Transactional
    public ResponseEntity<?> updateOrder(@PathVariable Long orderId, @RequestBody Map<String, Object> data) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.valueOf(data.get("status").toString());

        Long currentTableId = order.getTable() != null ? order.getTable().getId() : null;
        Long newTableId = data.get("tableId") != null ? Long.valueOf(data.get("tableId").toString()) : null;

        // ================================
        //  RULE 1: IN_PROGRESS → CANCELLED : Không cho phép
        // ================================
        if (oldStatus == OrderStatus.IN_PROGRESS && newStatus == OrderStatus.CANCELLED) {
            return ResponseEntity.badRequest().body("Đơn đã có món, không thể hủy!");
        }

        // ================================
        // RULE 2: Nếu trạng thái mới = COMPLETED hoặc CANCELLED → không cho đổi bàn
        // ================================
        boolean isFinishState = (newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED);

        if (isFinishState && newTableId != null && !newTableId.equals(currentTableId)) {
            return ResponseEntity.badRequest().body("Không thể chuyển bàn rồi hoàn thành/hủy đơn!");
        }

        // ================================
        // RULE 3: Xử lý đổi bàn (chỉ khi NEW hoặc IN_PROGRESS)
        // ================================
        if (newTableId != null && !newTableId.equals(currentTableId)) {

            // Trạng thái cho phép đổi bàn: NEW hoặc IN_PROGRESS
            if (newStatus == OrderStatus.NEW || newStatus == OrderStatus.IN_PROGRESS) {

                // trả bàn cũ
                if (order.getTable() != null) {
                    order.getTable().setStatus(TableStatus.INACTIVE);
                    tableRepository.save(order.getTable());
                }

                // kích hoạt bàn mới
                RestaurantTable newTable = tableRepository.findById(newTableId)
                        .orElseThrow(() -> new RuntimeException("Table not found"));

                newTable.setStatus(TableStatus.ACTIVE);
                tableRepository.save(newTable);

                order.setTable(newTable);
            }
            else {
                return ResponseEntity.badRequest().body("Không thể đổi bàn khi đơn đang hoàn thành hoặc đã hủy!");
            }
        }

        // ================================
        // RULE 4: Hoàn thành hoặc hủy → trả bàn về INACTIVE
        // ================================
        if (isFinishState) {
            if (order.getTable() != null) {
                order.getTable().setStatus(TableStatus.INACTIVE);
                tableRepository.save(order.getTable());
            }
        }

        // ================================
        // UPDATE STATUS
        // ================================
        order.setStatus(newStatus);

        // ================================
        // UPDATE CUSTOMER
        // ================================
        Long customerId = data.get("customerId") != null ? Long.valueOf(data.get("customerId").toString()) : null;
        if (customerId != null) {
            Customer c = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            order.setCustomer(c);
        }

        // ================================
        // UPDATE NOTE
        // ================================
        order.setNote(data.get("note") != null ? data.get("note").toString() : null);

        // ================================
        // UPDATE DISH ITEMS
        // ================================
        order.getOrderDetails().clear();

        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        for (Map<String, Object> item : items) {
            Long dishId = Long.valueOf(item.get("dishId").toString());
            int qty = Integer.parseInt(item.get("quantity").toString());

            Dish dish = dishRepository.findById(dishId)
                    .orElseThrow(() -> new RuntimeException("Dish not found"));

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .dish(dish)
                    .quantity(qty)
                    .unitPrice(dish.getUnitPrice())
                    .build();
            order.getOrderDetails().add(detail);
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        return ResponseEntity.ok("Cập nhật đơn hàng thành công!");
    }







//    @PutMapping("/{orderId}/update")
//    @Transactional
//    public ResponseEntity<?> updateOrder(@PathVariable Long orderId,
//                                         @RequestBody Map<String, Object> data) {
//
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        // ========== STATUS ==========
//        order.setStatus(OrderStatus.valueOf(data.get("status").toString()));
//
//        // ========== TABLE ==========
//        Long tableId = data.get("tableId") != null ? Long.valueOf(data.get("tableId").toString()) : null;
//
//        if (tableId != null && (order.getTable() == null || !order.getTable().getId().equals(tableId))) {
//
//            // trả bàn cũ
//            if (order.getTable() != null) {
//                order.getTable().setStatus(TableStatus.INACTIVE);
//                tableRepository.save(order.getTable());
//            }
//
//            RestaurantTable newTable = tableRepository.findById(tableId)
//                    .orElseThrow(() -> new RuntimeException("Table not found"));
//
//            newTable.setStatus(TableStatus.ACTIVE);
//            tableRepository.save(newTable);
//
//            order.setTable(newTable);
//        }
//
//        // ========== CUSTOMER ==========
//        Long customerId = data.get("customerId") != null ? Long.valueOf(data.get("customerId").toString()) : null;
//
//        if (customerId != null) {
//            // luôn dùng khách cũ, KHÔNG tạo khách mới
//            Customer c = customerRepository.findById(customerId)
//                    .orElseThrow(() -> new RuntimeException("Customer not found"));
//            order.setCustomer(c);
//        }
//
//        // ========== NOTE ==========
//        order.setNote(data.get("note") != null ? data.get("note").toString() : null);
//
//        // ========== ITEMS ==========
//        order.getOrderDetails().clear();
//
//        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
//
//        for (Map<String, Object> item : items) {
//            Long dishId = Long.valueOf(item.get("dishId").toString());
//            int qty = Integer.parseInt(item.get("quantity").toString());
//
//            Dish dish = dishRepository.findById(dishId)
//                    .orElseThrow(() -> new RuntimeException("Dish not found"));
//
//            OrderDetail detail = OrderDetail.builder()
//                    .order(order)
//                    .dish(dish)
//                    .quantity(qty)
//                    .unitPrice(dish.getUnitPrice())
//                    .build();
//
//            order.getOrderDetails().add(detail);
//        }
//
//        order.setUpdatedAt(LocalDateTime.now());
//        orderRepository.save(order);
//
//        return ResponseEntity.ok("Cập nhật đơn hàng thành công!");
//    }



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

    @GetMapping("/{orderId}/edit")
    public Map<String, Object> getOrderEditData(@PathVariable Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Map<String, Object> res = new HashMap<>();

        // BÀN HIỆN TẠI
        if (order.getTable() != null) {
            res.put("tableId", order.getTable().getId());
            res.put("tableName", order.getTable().getTableName());
        } else {
            res.put("tableId", null);
            res.put("tableName", "—");
        }

        // DANH SÁCH BÀN INACTIVE
        List<Map<String, Object>> tbl = new ArrayList<>();
        tableRepository.findAll().stream()
                .filter(t -> t.getStatus() == TableStatus.INACTIVE)
                .forEach(t -> tbl.add(Map.of(
                        "id", t.getId(),
                        "tableName", t.getTableName()
                )));

        // **THÊM BÀN HIỆN TẠI VÀO DANH SÁCH** nếu đang ACTIVE
        if (order.getTable() != null && order.getTable().getStatus() == TableStatus.ACTIVE) {
            tbl.add(0, Map.of(
                    "id", order.getTable().getId(),
                    "tableName", order.getTable().getTableName()
            ));
        }

        res.put("availableTables", tbl);

        // STATUS
        res.put("status", order.getStatus().name());

        // KHÁCH
        if (order.getCustomer() != null) {
            res.put("customerName", order.getCustomer().getFullName());
            res.put("customerPhone", order.getCustomer().getPhone());
        } else {
            res.put("customerName", "Khách vãng lai");
            res.put("customerPhone", "—");
        }

        // GHI CHÚ
        res.put("note", order.getNote());

        // MÓN ĂN
        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderDetail d : order.getOrderDetails()) {
            items.add(Map.of(
                    "dishId", d.getDish().getId(),
                    "dishName", d.getDish().getDishName(),
                    "quantity", d.getQuantity()
            ));
        }

        res.put("items", items);

        return res;
    }



    @GetMapping("/{orderId}/info")
    public Map<String, Object> getOrderInfo(@PathVariable Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Map<String, Object> res = new HashMap<>();

        res.put("orderId", order.getId());
        res.put("tableName", order.getTable() != null ? order.getTable().getTableName() : "—");

        if (order.getCustomer() != null) {
            res.put("customerName", order.getCustomer().getFullName());
            res.put("customerPhone", order.getCustomer().getPhone());
        } else {
            res.put("customerName", "Khách vãng lai");
            res.put("customerPhone", "—");
        }

        res.put("note", order.getNote());

        return res;
    }


    @DeleteMapping("/{orderId}")
    @Transactional
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // ==== KIỂM TRA TRẠNG THÁI CHO PHÉP XOÁ ====
        OrderStatus status = order.getStatus();

        if (status == OrderStatus.IN_PROGRESS) {
            return ResponseEntity.badRequest()
                    .body("Bàn đang được dùng (IN_PROGRESS), không thể xoá order này!");
        }

        if (status == OrderStatus.COMPLETED) {
            return ResponseEntity.badRequest()
                    .body("Order đã hoàn thành (COMPLETED), không thể xoá!");
        }

        // Chỉ cho xoá nếu NEW hoặc CANCELLED
        if (!(status == OrderStatus.NEW || status == OrderStatus.CANCELLED)) {
            return ResponseEntity.badRequest()
                    .body("Trạng thái không hợp lệ để xoá đơn hàng!");
        }

        // ==== XÓA ORDER ====

        // Xóa detail trước (tránh lỗi FK)
        order.getOrderDetails().clear();
        orderRepository.save(order);

        // Nếu order có gắn bàn → set lại trạng thái INACTIVE
        if (order.getTable() != null) {
            order.getTable().setStatus(TableStatus.INACTIVE);
            tableRepository.save(order.getTable());
        }

        // Xóa order
        orderRepository.delete(order);

        return ResponseEntity.ok("Xóa đơn hàng thành công!");
    }


}
