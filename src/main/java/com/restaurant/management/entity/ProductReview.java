package com.restaurant.management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ProductReview",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id","user_id","table_id"}))
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private RestaurantTable table;

    private Integer rating;

    @Column(name = "review_date")
    private LocalDateTime reviewDate = LocalDateTime.now();

    private String comment;

    public ProductReview() {}
}