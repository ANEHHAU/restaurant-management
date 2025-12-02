package com.restaurant.management.repository;

import com.restaurant.management.model.RestaurantTable;
import com.restaurant.management.model.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findBySeatsGreaterThanEqual(int seats);
}
