package com.restaurant.management.repository;

import com.restaurant.management.model.Reservation;
import com.restaurant.management.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

//    // Tìm bàn trống và trạng thái trống theo khung giờ khách tìm

}
