package com.example.demo.dto.res.Review;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewResponse {
    private int id;
    private String reviewText;
    private int rate;
    private LocalDateTime createAt;
    private String userName;
    private String productName;
}
