package com.restaurant.management.repository;

import com.restaurant.management.model.RestaurantTable;
import java.util.List;

import com.restaurant.management.model.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TableRepository extends JpaRepository<RestaurantTable, Long> {
    @Query("""
    SELECT t.id, SUM(od.unitPrice * od.quantity) 
    FROM OrderDetail od
    JOIN od.order o
    JOIN o.table t
    WHERE o.status = com.restaurant.management.model.OrderStatus.COMPLETED
    GROUP BY t.id
    """)
    List<Object[]> getRevenueByTable();


    @Query("""
    SELECT t.id, t.tableName, t.seats, t.status
    FROM RestaurantTable t
    ORDER BY t.id
    """)
    List<Object[]> findAllTables();


    // Trong RestaurantTableRepository.java
    List<RestaurantTable> findBySeatsGreaterThanEqualAndStatus(int seats, TableStatus status);



    @Query("SELECT t FROM RestaurantTable t WHERE t.status = 'INACTIVE'")
    List<RestaurantTable> findAllInActiveTables();


    @Query(value = """
        SELECT 
            t.id,
            t.table_name,
            t.seats,
            t.status,

            COALESCE((
                SELECT SUM(od.quantity * od.unit_price)
                FROM orders o
                JOIN order_detail od ON od.order_id = o.id
                WHERE o.table_id = t.id
                  AND o.status = 'COMPLETED'
            ), 0) AS revenue

        FROM restaurant_table t
        ORDER BY t.id
        """, nativeQuery = true)
    List<Object[]> findAllTableWithRevenue();

}
