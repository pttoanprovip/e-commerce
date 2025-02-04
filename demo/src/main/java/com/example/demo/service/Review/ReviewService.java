package com.example.demo.service.Review;

import java.util.List;

import com.example.demo.dto.req.Review.ReviewRequest;
import com.example.demo.dto.res.Review.ReviewResponse;

public interface ReviewService {
    ReviewResponse create(ReviewRequest reviewRequest);

    void delete(int id);

    ReviewResponse update(int id, ReviewRequest reviewRequest);

    List<ReviewResponse> getAll();

    List<ReviewResponse> getByProductId(int productId);


}
