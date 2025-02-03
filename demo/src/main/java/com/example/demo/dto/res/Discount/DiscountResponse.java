package com.example.demo.dto.res.Discount;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DiscountResponse {
    private int id;
    private String code;
    private double discountPercentage;
    private double maxDiscountAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive = true;
}
