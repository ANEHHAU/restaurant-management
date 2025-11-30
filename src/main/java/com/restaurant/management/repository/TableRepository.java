package com.restaurant.management.repository;

import com.restaurant.management.model.RestaurantTable;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TableRepository extends JpaRepository<RestaurantTable, Long> {



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
