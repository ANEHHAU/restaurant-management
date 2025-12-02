package com.restaurant.management.api;

import com.restaurant.management.model.*;
import com.restaurant.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
public class TableRestController {

    private final TableRepository tableRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final ReservationRepository reservationRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CustomerRepository customerRepository;

    @GetMapping("/list")
    public String listTables(Model model) {

        List<Object[]> rawTables = tableRepository.findAllTables();
        List<Object[]> revenueList = tableRepository.getRevenueByTable();

        Map<Long, Long> revenueMap = new HashMap<>();
        for (Object[] row : revenueList) {
            revenueMap.put((Long) row[0], (Long) row[1]);
        }

        List<Object[]> tables = new ArrayList<>();

        for (Object[] t : rawTables) {
            Long tableId = (Long) t[0];
            Long revenue = revenueMap.getOrDefault(tableId, 0L);

            // Tạo row mới: 0=id, 1=name, 2=seats, 3=status, 4=revenue
            Object[] newRow = new Object[5];
            newRow[0] = t[0];
            newRow[1] = t[1];
            newRow[2] = t[2];
            newRow[3] = t[3];
            newRow[4] = revenue;

            tables.add(newRow);
        }

        model.addAttribute("tables", tables);
        return "table/list";
    }


    @GetMapping("/available")
    public ResponseEntity<List<RestaurantTable>> getAvailableTables(
            @RequestParam("time")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkTime) {

        if (checkTime == null) {
            return ResponseEntity.badRequest().build();
        }

        List<RestaurantTable> availableTables = restaurantTableRepository.findAvailableTablesAt(checkTime);
        //                                                                    ↑ đúng tên method

        return ResponseEntity.ok(availableTables);
    }




    @GetMapping("/{id}/active-order")
    public ResponseEntity<?> getActiveOrder(@PathVariable Long id) {
        Optional<Order> order = orderRepository.findActiveOrderByTableId(id);

        if (order.isEmpty()) {
            return ResponseEntity.ok(0); // không có khách
        }

        return ResponseEntity.ok(order.get().getGuestCount());
    }



    // ============================
    // LẤY THÔNG TIN BÀN
    // ============================
    @GetMapping("/{id}")
    public ResponseEntity<?> getTable(@PathVariable Long id) {
        return tableRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().body("Không tìm thấy bàn"));
    }

    // ============================
    // UPDATE BÀN
    // ============================
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTable(@PathVariable Long id, @RequestBody RestaurantTable req) {

        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));

        // ================================
        // 1) KIỂM TRA RESERVATION CÒN HIỆU LỰC
        // ================================
        List<String> activeRes = List.of("PENDING", "CONFIRMED");

        Long countReservation = reservationRepository.countActiveReservation(id, activeRes);
        boolean hasActiveReservation = countReservation > 0;

        // ================================
        // 2) KIỂM TRA ORDER ĐANG HOẠT ĐỘNG
        // ================================
        List<OrderStatus> activeOrders = List.of(OrderStatus.NEW, OrderStatus.IN_PROGRESS);

        Long countOrders = orderRepository.countActiveOrders(id, activeOrders);
        boolean hasActiveOrder = countOrders > 0;

        // ================================
        // 3) KIỂM TRA SỐ GHẾ CÓ THAY ĐỔI KHÔNG
        // ================================
        boolean isSeatChanged = table.getSeats() != req.getSeats();

        if (isSeatChanged && (hasActiveReservation || hasActiveOrder)) {
            return ResponseEntity.badRequest()
                    .body("Bàn đang được sử dụng hoặc có lịch đặt — không thể sửa số ghế!");
        }

        // ================================
        // 4) CẬP NHẬT THÔNG TIN (Chỉ sửa được tên)
        // ================================
        table.setTableName(req.getTableName());

        // CHỈ CHO PHÉP ĐỔI GHẾ NẾU KHÔNG CÓ RÀNG BUỘC
        if (!hasActiveReservation && !hasActiveOrder) {
            table.setSeats(req.getSeats());
        }

        tableRepository.save(table);

        return ResponseEntity.ok("Cập nhật bàn thành công!");
    }


    // ============================
    // DELETE BÀN
    // ============================
    @DeleteMapping("/delete/{id}")
    @Transactional
    public ResponseEntity<?> deleteTable(@PathVariable Long id) {

        // 1) Check bàn tồn tại
        RestaurantTable table = tableRepository.findById(id).orElse(null);
        if (table == null) {
            return ResponseEntity.badRequest().body("Bàn không tồn tại");
        }

        // 2) Check reservation còn hiệu lực
        List<String> activeRes = List.of("PENDING", "CONFIRMED");
        Long countRes = reservationRepository.countActiveReservation(id, activeRes);
        if (countRes != null && countRes > 0) {
            return ResponseEntity.badRequest()
                    .body("Bàn đang được khách đặt trước — không thể xóa!");
        }

        // 3) Check order đang sử dụng
        List<OrderStatus> activeOrders = List.of(OrderStatus.NEW, OrderStatus.IN_PROGRESS);
        Long countOrders = orderRepository.countActiveOrders(id, activeOrders);
        if (countOrders != null && countOrders > 0) {
            return ResponseEntity.badRequest()
                    .body("Bàn đang có khách sử dụng — không thể xóa!");
        }

        // ----------------------------------------------------
        // Đến đây chỉ còn lịch sử: reservation COMPLETED/CANCELLED, order COMPLETED/CANCELLED
        // ----------------------------------------------------

        // 4) Gỡ FK Order -> Reservation (theo bàn này)
        orderRepository.clearReservationByTableId(id);

        // 5) Xóa tất cả reservation của bàn này
        reservationRepository.deleteAllByTableId(id);

        // 6) Gỡ FK Order -> Table
        orderRepository.clearTableReference(id);

        // 7) Xóa bàn
        tableRepository.delete(table);

        return ResponseEntity.ok("Đã xóa bàn thành công!");
    }




    // ============================
    // LẤY BILL THEO BÀN (TRẢ VỀ MAP)
    // ============================
    @GetMapping("/{id}/bill")
    public ResponseEntity<?> getBill(@PathVariable Long id) {

        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));

        Order order = orderRepository.findActiveOrderByTable(id)
                .orElseThrow(() -> new RuntimeException("Bàn chưa có order đang hoạt động"));

        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderDetail d : order.getOrderDetails()) {

            Map<String, Object> line = new HashMap<>();
            line.put("dishName", d.getDish().getDishName());
            line.put("quantity", d.getQuantity());
            line.put("unitPrice", d.getUnitPrice());
            line.put("lineTotal", d.getLineTotal());

            total = total.add(d.getLineTotal());
            items.add(line);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tableId", table.getId());
        result.put("tableName", table.getTableName());
        result.put("items", items);
        result.put("total", total);

        return ResponseEntity.ok(result);
    }

    // ============================
    // XÁC NHẬN THANH TOÁN
    // ============================
    @PostMapping("/{id}/pay")
    public ResponseEntity<?> payTable(@PathVariable Long id) {

        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));

        Order order = orderRepository.findActiveOrderByTable(id)
                .orElseThrow(() -> new RuntimeException("Không có order đang hoạt động"));

        // cập nhật trạng thái hóa đơn
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);



        if (order.getReservation() != null) {

            Reservation reservation = order.getReservation();

            // chỉ cập nhật nếu đang CONFIRMED hoặc IN_USE
            if (reservation.getStatus() == ReservationStatus.CONFIRMED ||
                    reservation.getStatus() == ReservationStatus.IN_USE) {

                reservation.setStatus(ReservationStatus.COMPLETED);
                reservationRepository.save(reservation);
            }
        }



        // reset trạng thái bàn → INACTIVE
        table.setStatus(TableStatus.INACTIVE);
        tableRepository.save(table);

        return ResponseEntity.ok("Thanh toán thành công");
    }






    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody RestaurantTable table) {
        if (table.getTableName() == null || table.getTableName().isBlank()) {
            return ResponseEntity.badRequest().body("Tên bàn không được để trống!");
        }

        if (table.getSeats() <= 0) {
            return ResponseEntity.badRequest().body("Số ghế không hợp lệ!");
        }

        if (table.getStatus() == null) {
            table.setStatus(TableStatus.INACTIVE);
        }

        tableRepository.save(table);
        return ResponseEntity.ok("OK");
    }


    @GetMapping("/{tableId}/schedule")
    public List<Map<String, Object>> getSchedule(@PathVariable Long tableId) {

        return reservationRepository.findScheduleByTableId(tableId)
                .stream()
                .map(row -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("start", row[0].toString());
                    m.put("end", row[1].toString());
                    m.put("customer", row[2]);
                    m.put("phone", row[3]);
                    m.put("status", row[4]);
                    return m;
                })
                .toList();
    }



//    @GetMapping("/{id}/print")
//    public ResponseEntity<String> printBill(@PathVariable Long id) {
//
//        Order order = orderRepository.findActiveOrderByTable(id)
//                .orElseThrow(() -> new RuntimeException("Không có hóa đơn cần in"));
//
//        List<OrderDetail> details = orderDetailRepository.findByOrder(order);
//
//        LocalDateTime now = LocalDateTime.now();
//        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
//
//        BigDecimal total = BigDecimal.ZERO;
//
//        StringBuilder html = new StringBuilder();
//        html.append("<html><head><meta charset='UTF-8'><title>Hóa đơn</title></head><body>");
//        html.append("<h2>HÓA ĐƠN THANH TOÁN – BÀN ").append(order.getTable().getTableName()).append("</h2>");
//
//        html.append("<p><b>Thời gian lập:</b> ").append(now.format(fmt)).append("</p>");
//
//        html.append("<table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse;width:100%;'>");
//        html.append("<tr><th>STT</th><th>Món ăn</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr>");
//
//        int index = 1;
//        for (OrderDetail d : details) {
//            BigDecimal line = d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity()));
//            total = total.add(line);
//
//            html.append("<tr>")
//                    .append("<td>").append(index++).append("</td>")
//                    .append("<td>").append(d.getDish().getDishName()).append("</td>")
//                    .append("<td>").append(d.getQuantity()).append("</td>")
//                    .append("<td>").append(d.getUnitPrice()).append("</td>")
//                    .append("<td>").append(line).append("</td>")
//                    .append("</tr>");
//        }
//
//        BigDecimal vat = total.multiply(BigDecimal.valueOf(0.1));
//        BigDecimal finalTotal = total.add(vat);
//
//        html.append("</table>");
//        html.append("<h3>Tổng tiền: ").append(total).append(" ₫</h3>");
//        html.append("<h3>VAT (10%): ").append(vat).append(" ₫</h3>");
//        html.append("<h2>Thành tiền: ").append(finalTotal).append(" ₫</h2>");
//
//        html.append("</body></html>");
//
//        return ResponseEntity.ok()
//                .header("Content-Type", "text/html; charset=UTF-8")
//                .body(html.toString());
//    }



}
