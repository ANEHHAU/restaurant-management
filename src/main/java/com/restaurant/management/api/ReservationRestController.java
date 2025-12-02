package com.restaurant.management.api;

import com.restaurant.management.model.*;
import com.restaurant.management.repository.CustomerRepository;
import com.restaurant.management.repository.ReservationRepository;
import com.restaurant.management.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationRestController {

    private final CustomerRepository customerRepository;
    private final RestaurantTableRepository tableRepo;
    private final ReservationRepository reservationRepo;


    @PostMapping("/cancel/{id}")
    public ResponseEntity<String> cancel(@PathVariable Long id) {
        try {
            Reservation r = reservationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));

            if (r.getStatus() == ReservationStatus.CANCELLED) {
                return ResponseEntity.ok("ALREADY_CANCELLED");
            }

            r.setStatus(ReservationStatus.CANCELLED);

            // r.setCancelledBy(currentUser); // nếu có login
            reservationRepo.save(r);

            return ResponseEntity.ok("SUCCESS");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }





    @PostMapping("/confirm/{id}")
    public ResponseEntity<String> confirmReservation(@PathVariable Long id) {
        try {
            Reservation reservation = reservationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));

            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                return ResponseEntity.ok("ALREADY_CONFIRMED");
            }

            if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                return ResponseEntity.badRequest().body("Đã hủy, không thể xác nhận");
            }

            reservation.setStatus(ReservationStatus.CONFIRMED);
            // Tùy anh muốn set nhân viên xác nhận:
            // reservation.setConfirmedBy(currentUser);

            reservationRepo.save(reservation);

            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }






    // ================== 1. TÌM BÀN TRỐNG ==================
    @PostMapping("/available")
    public ResponseEntity<List<RestaurantTable>> getAvailableTables(@RequestBody Map<String, Object> req) {
        String date = (String) req.get("date");           // yyyy-MM-dd
        String timeStart = (String) req.get("timeStart"); // HH:mm
        int numPeople = ((Number) req.get("numPeople")).intValue();

        LocalDateTime target = LocalDateTime.parse(date + "T" + timeStart);
        LocalDateTime from = target.minusHours(1);
        LocalDateTime to = target.plusHours(1);

        // Lấy danh sách bàn đã đặt trong khoảng ±1h
        List<Long> bookedTableIds = reservationRepo.findBookedTableIdsInRange(from, to);

        // Lấy tất cả bàn đủ ghế + đang ACTIVE + chưa bị đặt
        List<RestaurantTable> available = tableRepo
                .findBySeatsGreaterThanEqual(numPeople)
                .stream()
                .filter(t -> !bookedTableIds.contains(t.getId()))
                .toList();

        return ResponseEntity.ok(available);
    }



    @PostMapping("/confirm")
    public ResponseEntity<String> confirmBooking(@RequestBody Map<String, Object> req) {
        try {
            String date = getString(req, "date");
            String timeStart = getString(req, "timeStart");
            int numPeople = getInt(req, "numPeople");
            long tableId = getLong(req, "selectedTableId");
            String note = getString(req, "note");
            Long customerId = getLongOrNull(req, "customerId");

            Customer customer;
            if (customerId == null) {
                // Khách mới
                @SuppressWarnings("unchecked")
                Map<String, Object> newCust = (Map<String, Object>) req.get("newCustomer");
                if (newCust == null) {
                    return ResponseEntity.badRequest().body("Thiếu thông tin khách hàng mới");
                }

                String fullName = getString(newCust, "fullName");
                String phone = getString(newCust, "phone");
                String address = getString(newCust, "address");

                if (customerRepository.existsByPhone(phone)) {
                    return ResponseEntity.badRequest().body("Số điện thoại đã tồn tại");
                }

                customer = Customer.builder()
                        .fullName(fullName)
                        .phone(phone)
                        .address(address)
                        .build();
                customer = customerRepository.save(customer);
            } else {
                customer = customerRepository.findById(customerId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
            }

            RestaurantTable table = tableRepo.findById(tableId)
                    .orElseThrow(() -> new RuntimeException("Bàn không tồn tại"));

            LocalDateTime start = LocalDateTime.parse(date + "T" + timeStart + ":00");
            LocalDateTime end = start.plusHours(2);

            if (reservationRepo.existsByTableAndTimeOverlap(table, start, end)) {
                return ResponseEntity.badRequest().body("Bàn đã được đặt vào khung giờ này");
            }

            Reservation reservation = Reservation.builder()
                    .customer(customer)
                    .table(table)
                    .reservationStart(start)
                    .reservationEnd(end)
                    .numPeople(numPeople)
                    .note(note)
                    .status(ReservationStatus.PENDING)
                    .build();

            reservationRepo.save(reservation);
            return ResponseEntity.ok("SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // ================== CÁC HÀM HỖ TRỢ SIÊU AN TOÀN ==================
    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        return val instanceof String ? (String) val : String.valueOf(val);
    }

    private int getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) throw new IllegalArgumentException(key + " không được để trống");
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(val.toString());
    }

    private long getLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) throw new IllegalArgumentException(key + " không được để trống");
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private Long getLongOrNull(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        String str = val.toString().trim();
        return str.isEmpty() ? null : Long.parseLong(str);
    }



















    // ================== 2. XÁC NHẬN ĐẶT BÀN ==================
//    @PostMapping("/confirm")
//    public ResponseEntity<String> confirmBooking(@RequestBody Map<String, Object> req) {
//        try {
//            Long customerId = req.get("customerId") != null ? ((Number) req.get("customerId")).longValue() : null;
//            String date = (String) req.get("date");
//            String timeStart = (String) req.get("timeStart");
//
//
//
//            int numPeople = Integer.parseInt((String) req.get("numPeople"));
//            Long tableId = Long.parseLong((String) req.get("selectedTableId"));
//
//
//            String note = req.get("note") != null ? (String) req.get("note") : null;
//
//            Customer customer;
//            // Nếu là khách mới
//            if (customerId == null) {
//                @SuppressWarnings("unchecked")
//                Map<String, String> newCust = (Map<String, String>) req.get("newCustomer");
//                String phone = newCust.get("phone");
//
//                if (customerRepository.existsByPhone(phone)) {
//                    return ResponseEntity.badRequest().body("Số điện thoại đã tồn tại");
//                }
//
//                customer = Customer.builder()
//                        .fullName(newCust.get("fullName"))
//                        .phone(phone)
//                        .address(newCust.get("address"))
//                        .build();
//                customer = customerRepository.save(customer);
//            } else {
//                customer = customerRepository.findById(customerId)
//                        .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
//            }
//
//            RestaurantTable table = tableRepo.findById(tableId)
//                    .orElseThrow(() -> new RuntimeException("Bàn không tồn tại"));
//
//            LocalDateTime start = LocalDateTime.parse(date + "T" + timeStart);
//            LocalDateTime end = start.plusHours(2); // dùng bàn 2 tiếng
//
//            // Check trùng giờ
//            boolean conflict = reservationRepo.existsByTableAndTimeOverlap(table, start, end);
//            if (conflict) {
//                return ResponseEntity.badRequest().body("Bàn đã được đặt vào khung giờ này");
//            }
//
//            // Tạo đặt bàn
//            Reservation reservation = Reservation.builder()
//                    .customer(customer)
//                    .table(table)
//                    .reservationStart(start)
//                    .reservationEnd(end)
//                    .numPeople(numPeople)
//                    .note(note)
//                    .status(ReservationStatus.PENDING)
//                    .build();
//
//            reservationRepo.save(reservation);
//            return ResponseEntity.ok("SUCCESS");
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    @GetMapping("/findCustomer")
    public CustomerResponse findCustomerByPhone(@RequestParam String phone) {

        return customerRepository.findByPhone(phone)
                .map(c -> new CustomerResponse(c.getId(), c.getFullName()))
                .orElse(new CustomerResponse(null, null));
    }



    // DTO response
    public record CustomerResponse(Long id, String name) {}
}
