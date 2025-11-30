package com.restaurant.management.api;

import com.restaurant.management.model.Customer;
import com.restaurant.management.model.Order;
import com.restaurant.management.model.OrderStatus;
import com.restaurant.management.repository.CustomerRepository;
import com.restaurant.management.repository.OrderDetailRepository;
import com.restaurant.management.repository.OrderRepository;
import com.restaurant.management.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;


import com.restaurant.management.model.*;

import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerRestController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    // Tìm khách theo tên hoặc số điện thoại
    @GetMapping("/search")
    public List<Customer> search(@RequestParam String keyword) {
        return customerRepository.searchByKeyword(keyword);
    }

    // Lấy thông tin theo ID
    @GetMapping("/{id}")
    public Customer findCustomer(@PathVariable Long id) {
        return customerRepository.findById(id)
                .orElse(null);
    }

    // Update thông tin
    @PutMapping("/update")
    public Customer updateCustomer(@RequestBody Customer req) {
        Customer c = customerRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        c.setFullName(req.getFullName());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());

        return customerRepository.save(c);
    }


    @DeleteMapping("/delete/{id}")
    @Transactional
    public void deleteCustomer(@PathVariable Long id) {

        if (!customerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng");
        }

        // 1. Xóa chi tiết đơn
        List<Order> orders = orderRepository.findByCustomerId(id);

        if (!orders.isEmpty()) {
            List<Long> ids = orders.stream().map(Order::getId).toList();
            orderDetailRepository.deleteByOrderIds(ids);
            orderRepository.deleteByCustomerId(id);
        }

        // 2. Xóa đặt bàn
        reservationRepository.deleteByCustomerId(id);

        // 3. Xóa khách
        customerRepository.deleteById(id);
    }




    @GetMapping("/{id}/orders")
    public Map<String, Object> getCustomerOrders(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));

        // Lấy các order COMPLETED của khách này
        List<Order> orders = orderRepository.findByCustomerAndStatus(customer, OrderStatus.COMPLETED);

        // Tổng tiền đã tiêu
        BigDecimal totalSpent = BigDecimal.ZERO;

        // Đếm số lần ăn theo từng món
        Map<String, Long> dishCount = new HashMap<>();

        List<Map<String, Object>> orderDtos = new ArrayList<>();

        for (Order order : orders) {
            BigDecimal orderTotal = BigDecimal.ZERO;

            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {

                    // Tính subtotal dòng chi tiết
                    BigDecimal lineTotal = detail.getLineTotal(); // đã có trong model của bạn
                    if (lineTotal != null) {
                        orderTotal = orderTotal.add(lineTotal);
                    }

                    // Đếm món yêu thích
                    Dish dish = detail.getDish();
                    if (dish != null) {
                        String dishName = dish.getDishName();
                        long qty = detail.getQuantity();

                        dishCount.put(
                                dishName,
                                dishCount.getOrDefault(dishName, 0L) + qty
                        );
                    }
                }
            }

            totalSpent = totalSpent.add(orderTotal);

            // DTO cho từng order
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", order.getId());
            dto.put("tableName",
                    order.getTable() != null ? order.getTable().getTableName() : null);
            dto.put("time", order.getOrderTime());
            dto.put("total", orderTotal);

            orderDtos.add(dto);
        }

        // Tìm món yêu thích
        String favoriteDish = "Không có dữ liệu";
        long favoriteCount = 0;

        if (!dishCount.isEmpty()) {
            Map.Entry<String, Long> maxEntry = dishCount.entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            if (maxEntry != null) {
                favoriteDish = maxEntry.getKey();
                favoriteCount = maxEntry.getValue();
            }
        }

        // Build response JSON
        Map<String, Object> res = new HashMap<>();
        res.put("customerName", customer.getFullName());
        res.put("totalSpent", totalSpent);
        res.put("favoriteDish", favoriteDish);
        res.put("favoriteCount", favoriteCount);
        res.put("orders", orderDtos);

        return res;
    }

}
