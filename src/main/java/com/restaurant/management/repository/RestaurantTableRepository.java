package com.restaurant.management.repository;

import com.restaurant.management.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Integer> {

    @Query("""
    SELECT t FROM RestaurantTable t
    WHERE t.capacity >= :guests
    AND t.tableId NOT IN (
        SELECT c.table.tableId FROM TableCalendar c
        WHERE c.startTime < :endTime
        AND c.endTime > :startTime
        AND c.status <> 'Free'
    )
    """)
    List<RestaurantTable> findAvailableTables(
            @Param("guests") int guests,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}