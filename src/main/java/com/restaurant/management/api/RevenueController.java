package com.restaurant.management.api;

import com.restaurant.management.model.Order;
import com.restaurant.management.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final OrderRepository orderRepository;

    @GetMapping
    public String showRevenuePage(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,

            Model model
    ) {

        // Lần đầu vào trang → chưa chọn ngày
        if (from == null || to == null) {
            model.addAttribute("orders", null);
            model.addAttribute("totalRevenue", null);
            return "revenue/revenue-page";
        }

        // Lấy danh sách đơn hoàn thành trong khoảng thời gian
        List<Order> orders = orderRepository.findCompletedOrdersBetween(from, to);

        // Tính tổng doanh thu
        long total = orders.stream()
                .mapToLong(o -> o.getOrderDetails().stream()
                        .map(d -> d.getUnitPrice().longValue() * d.getQuantity())
                        .reduce(0L, Long::sum))
                .sum();

        model.addAttribute("orders", orders);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("totalRevenue", total);

        return "revenue/revenue-page";
    }
}
