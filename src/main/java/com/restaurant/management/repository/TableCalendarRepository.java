package com.restaurant.management.repository;

import com.restaurant.management.entity.TableCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableCalendarRepository extends JpaRepository<TableCalendar, Integer> {
}