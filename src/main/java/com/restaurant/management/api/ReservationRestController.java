package com.restaurant.management.api;

import com.restaurant.management.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationRestController {

    private final CustomerRepository customerRepository;

    @GetMapping("/findCustomer")
    public CustomerResponse findCustomerByPhone(@RequestParam String phone) {

        return customerRepository.findByPhone(phone)
                .map(c -> new CustomerResponse(c.getId(), c.getFullName()))
                .orElse(new CustomerResponse(null, null));
    }

    // DTO response
    public record CustomerResponse(Long id, String name) {}
}
