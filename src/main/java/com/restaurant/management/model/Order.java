package com.restaurant.management.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nhiều order thuộc 1 khách
//    @ManyToOne
//    @JoinColumn(name = "customer_id")
//    @JsonIgnore
//    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonBackReference  // ← Thay @JsonIgnore bằng cái này
    private Customer customer;


    // Nhiều order thuộc 1 bàn
    @ManyToOne
    @JoinColumn(name = "table_id")
    @JsonIgnore
    private RestaurantTable table;

    // Nhiều order có thể gắn với 1 reservation
    @ManyToOne
    @JoinColumn(name = "reservation_id")
    @JsonIgnore
    private Reservation reservation;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @Column(name = "guest_count")
    private Integer guestCount;

    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

//    @OneToMany(mappedBy = "order",  orphanRemoval = true,cascade = CascadeType.REMOVE)
//    @JsonIgnore
//    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OrderDetail> orderDetails = new ArrayList<>();


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (orderTime == null) {
            orderTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }


    // Tính tổng tiền của order bằng cách cộng tất cả lineTotal
    public BigDecimal getTotalAmount() {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return orderDetails.stream()
                .map(OrderDetail::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
