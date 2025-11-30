package com.restaurant.management.repository;

import com.restaurant.management.model.Reservation;
import com.restaurant.management.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByTableId(Long tableId);
    @Query("""
    SELECT 
        r.reservationStart,
        r.reservationEnd,
        r.customer.fullName,
        r.customer.phone,
        r.status
    FROM Reservation r
    WHERE r.table.id = :tableId
    ORDER BY r.reservationStart
""")
    List<Object[]> findScheduleByTableId(@Param("tableId") Long tableId);

    @Modifying
    @Transactional
    void deleteByCustomerId(Long customerId);

    List<Reservation> findByCustomerId(Long customerId);

}
