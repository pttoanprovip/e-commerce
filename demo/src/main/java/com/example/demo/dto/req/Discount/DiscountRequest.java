package com.example.demo.dto.req.Discount;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DiscountRequest {
    private String code;
    private double discountPercentage;
    private double maxDiscountAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive = true;
    private boolean used = false;
}
