package com.example.demo.dto.req.Statistic;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class StatisticRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
