package com.restaurant.management.repository;

import com.restaurant.management.entity.Product;
import com.restaurant.management.entity.ProductReview;
import com.restaurant.management.entity.RestaurantTable;
import com.restaurant.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Integer> {

    boolean existsByProductAndUserAndTable(
            Product product,
            User user,
            RestaurantTable table);
}