package com.restaurant.management.repository;

import com.restaurant.management.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM OrderDetail od WHERE od.order.id IN :orderIds")
    void deleteByOrderIds(List<Long> orderIds);

    void deleteByOrderId(Long orderId);

}
