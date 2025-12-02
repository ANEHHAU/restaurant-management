package com.restaurant.management.repository;

import com.restaurant.management.model.Customer;
import com.restaurant.management.model.Order;
import com.restaurant.management.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerAndStatus(Customer customer, OrderStatus status);

    @Query("""
    SELECT o FROM Order o
    WHERE o.status = com.restaurant.management.model.OrderStatus.COMPLETED
      AND o.orderTime BETWEEN :from AND :to
    ORDER BY o.orderTime DESC
    """)
    List<Order> findCompletedOrdersBetween(LocalDateTime from, LocalDateTime to);


    List<Order> findByCustomerId(Long customerId);


    @Query("SELECT o FROM Order o WHERE o.table.id = :tableId AND o.status IN ('NEW','IN_PROGRESS')")
    Optional<Order> findActiveOrderByTableId(@Param("tableId") Long tableId);



    @Query("SELECT o FROM Order o WHERE o.orderTime BETWEEN :from AND :to ORDER BY o.orderTime DESC")
    List<Order> findOrdersBetween(@Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);


    @Modifying
    @Transactional
    void deleteByCustomerId(Long customerId);


    // LẤY HÓA ĐƠN CHƯA HOÀN TẤT CỦA MỘT BÀN (hóa đơn đang hoạt động)
    @Query("""
    SELECT o FROM Order o 
    WHERE o.table.id = :tableId 
      AND o.status IN (com.restaurant.management.model.OrderStatus.NEW, 
                       com.restaurant.management.model.OrderStatus.IN_PROGRESS)
    ORDER BY o.orderTime ASC
    """)
    Optional<Order> findActiveOrderByTable(Long tableId);

    // LẤY HÓA ĐƠN ĐÃ HOÀN TẤT (dùng khi in lại hoặc tra cứu)
    @Query("""
        SELECT o FROM Order o 
        WHERE o.table.id = :tableId 
          AND o.status = 'COMPLETED'
        ORDER BY o.orderTime DESC
        """)
    Optional<Order> findLastCompletedOrderByTable(Long tableId);

    @Query("""
           SELECT COUNT(o)
           FROM Order o
           WHERE o.table.id = :tableId
             AND o.status IN :statuses
           """)
    Long countActiveOrders(@Param("tableId") Long tableId,
                           @Param("statuses") List<OrderStatus> statuses);

    // Gỡ FK Order -> Table
    @Modifying
    @Query("""
           UPDATE Order o
           SET o.table = NULL
           WHERE o.table.id = :tableId
           """)
    int clearTableReference(@Param("tableId") Long tableId);

    // Gỡ FK Order -> Reservation (theo bàn)
    @Modifying
    @Query("""
           UPDATE Order o
           SET o.reservation = NULL
           WHERE o.reservation.table.id = :tableId
           """)
    int clearReservationByTableId(@Param("tableId") Long tableId);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.table.id = :tableId AND o.status = 'ACTIVE'")
    Optional<Order> findActiveOrderByCustomerAndTable(@Param("customerId") Long customerId,
                                                      @Param("tableId") Long tableId);


}