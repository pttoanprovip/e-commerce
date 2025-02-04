package com.example.demo.dto.req.Review;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Data;

@Data
public class ReviewRequest {
    private int userId;
    private int productId;
    private String reviewText;
    @Min(1)
    @Max(5)
    private int rate;
}
