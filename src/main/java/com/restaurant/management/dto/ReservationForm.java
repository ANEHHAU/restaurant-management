package com.restaurant.management.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReservationForm {
    private String phone;
    private String customerName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime timeStart;

    private Integer numPeople;

    private Long selectedTableId;
}
