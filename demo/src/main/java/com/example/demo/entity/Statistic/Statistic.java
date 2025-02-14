package com.example.demo.entity.Statistic;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    // @Column(name = "total_user")
    // private int totalUser = 0;

    @Column(name = "total_orders")
    private int totalOrder = 0;

    @Column(name = "total_revenue")
    private double totalRevenue = 0.0;

    @Column(name = "total_products_sold")
    private int totalProductSold = 0;

    @Column(name = "created_at")
    private LocalDateTime createAt = LocalDateTime.now();
}
