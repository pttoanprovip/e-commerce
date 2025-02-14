package com.example.demo.dto.res.Statistic;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class StatisticResponse {
    private int id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    //private int totalUser;
    private int totalOrder;
    private double totalRevenue;
    private int totalProductSold;
    private LocalDateTime createAt;
}
