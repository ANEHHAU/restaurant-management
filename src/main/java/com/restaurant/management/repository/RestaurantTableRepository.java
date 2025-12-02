package com.restaurant.management.repository;

import com.restaurant.management.model.RestaurantTable;
import com.restaurant.management.model.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findBySeatsGreaterThanEqual(int seats);



    @Query("""
        SELECT t FROM RestaurantTable t
        WHERE NOT EXISTS (
            SELECT 1 FROM Reservation r
            WHERE r.table = t
              AND r.status IN ('PENDING', 'CONFIRMED', 'IN_USE')
              AND r.reservationStart <= :checkTime
              AND r.reservationEnd > :checkTime
        )
        ORDER BY t.tableName
        """)
    List<RestaurantTable> findAvailableTablesAt(@Param("checkTime") LocalDateTime checkTime);
}
