package com.restaurant.management.api;

import com.restaurant.management.model.Customer;
import com.restaurant.management.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerRestController {

    private final CustomerRepository customerRepository;

    // GET /customers/list
    @GetMapping("/list")
    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {

        if (customer.getFullName() == null || customer.getFullName().isBlank()) {
            return ResponseEntity.badRequest().body("Tên khách hàng không được để trống");
        }

        if (customer.getPhone() == null || customer.getPhone().isBlank()) {
            return ResponseEntity.badRequest().body("Số điện thoại không được để trống");
        }

        // Regex số điện thoại VN: 10 số, bắt đầu bằng 0
        if (!customer.getPhone().matches("^0\\d{9}$")) {
            return ResponseEntity.badRequest().body("Số điện thoại không hợp lệ (phải gồm 10 số và bắt đầu bằng 0)");
        }

        if (customerRepository.findByPhone(customer.getPhone()).isPresent()) {
            return ResponseEntity.badRequest().body("Số điện thoại đã tồn tại trong hệ thống");
        }

        Customer saved = customerRepository.save(customer);
        return ResponseEntity.ok(saved);
    }

}
