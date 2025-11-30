package com.restaurant.management.api;

import com.restaurant.management.model.RestaurantTable;
import com.restaurant.management.repository.ReservationRepository;
import com.restaurant.management.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
public class TableRestController {

    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;


    @GetMapping("/list")
    public List<Object[]> list() {
        return tableRepository.findAllTableWithRevenue();
    }

    @GetMapping("/{tableId}/schedule")
    public List<Map<String, Object>> getSchedule(@PathVariable Long tableId) {

        return reservationRepository.findScheduleByTableId(tableId)
                .stream()
                .map(row -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("start", row[0].toString());
                    m.put("end", row[1].toString());
                    m.put("customer", row[2]);
                    m.put("phone", row[3]);
                    m.put("status", row[4]);
                    return m;
                })
                .toList();
    }


}
