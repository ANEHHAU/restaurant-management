package com.restaurant.management.api;

import com.restaurant.management.model.RestaurantTable;
import com.restaurant.management.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
public class TableRestController {

    private final RestaurantTableRepository tableRepository;

    // GET /tables/list
    @GetMapping("/list")
    public List<RestaurantTable> listTables() {
        return tableRepository.findAll();
    }
}
