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
    @Query("""
    SELECT r FROM Reservation r
    WHERE 
        (r.status = com.restaurant.management.model.ReservationStatus.PENDING
         OR r.status = com.restaurant.management.model.ReservationStatus.CONFIRMED)
    AND r.reservationStart BETWEEN :startOfDay AND :endOfDay
    ORDER BY r.reservationStart ASC
""")
    List<Reservation> findActiveReservationsForToday(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );





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


    @Query("""
           SELECT COUNT(r)
           FROM Reservation r
           WHERE r.table.id = :tableId
             AND r.status IN :statuses
           """)
    Long countActiveReservation(@Param("tableId") Long tableId,
                                @Param("statuses") List<String> statuses);

    @Modifying
    @Query("""
           DELETE FROM Reservation r
           WHERE r.table.id = :tableId
           """)
    int deleteAllByTableId(@Param("tableId") Long tableId);
}


