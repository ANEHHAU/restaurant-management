// src/main/java/com/restaurant/management/controller/api/DishApiController.java
package com.restaurant.management.api;

import com.restaurant.management.model.Dish;
import com.restaurant.management.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dishes")
public class DishApiController {

    @Autowired
    private DishRepository dishRepository;

    @GetMapping
    public List<Dish> getAll() {
        return dishRepository.findAllOrderByCreatedAtDesc();
    }


    @GetMapping("/listCategories")
    public List<String> listCategories() {
        List<String> categories = dishRepository.findDistinctCategories();
        categories.sort(String::compareToIgnoreCase);
        return categories;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dish> getById(@PathVariable Long id) {
        return dishRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Dish> create(@RequestBody Dish dish) {
        dish.setCreatedAt(LocalDateTime.now());
        dish.setUpdatedAt(LocalDateTime.now());
        Dish saved = dishRepository.save(dish);
        return ResponseEntity.ok(saved);
    }





    @PutMapping("/update")
    public ResponseEntity<Dish> update(@RequestBody Dish dish) {
        if (dish.getId() == null || !dishRepository.existsById(dish.getId())) {
            return ResponseEntity.notFound().build();
        }
        dish.setUpdatedAt(LocalDateTime.now());
        Dish updated = dishRepository.save(dish);
        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return dishRepository.findById(id)
                .map(dish -> {
                    // Nếu món đã có trong hóa đơn → không cho xóa thật, chỉ ẩn đi
                    if (!dish.getOrderDetails().isEmpty()) {
                        dish.setActive(false);  // tạm ẩn
                        dishRepository.save(dish);
                        return ResponseEntity.ok(Map.of("message", "Món đã được ẩn vì có trong hóa đơn!"));
                    }

                    // Nếu chưa có hóa đơn nào → xóa thật
                    dishRepository.delete(dish);
                    return ResponseEntity.ok(Map.of("message", "Xóa món thành công!"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}