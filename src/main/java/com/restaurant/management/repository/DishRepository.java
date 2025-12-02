package com.restaurant.management.repository;

import com.restaurant.management.model.Customer;
import com.restaurant.management.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DishRepository extends JpaRepository<Dish, Long> {

    @Query("SELECT d FROM Dish d ORDER BY d.createdAt DESC")
    List<Dish> findAllOrderByCreatedAtDesc();
//món mới lên đầu

    @Query("SELECT DISTINCT d.category FROM Dish d WHERE d.category IS NOT NULL")
    List<String> findDistinctCategories();
//tìm danh mục lọc trùng

    List<Dish> findByActiveTrueOrderByDishNameAsc();

}
