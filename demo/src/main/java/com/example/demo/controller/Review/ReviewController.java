package com.example.demo.controller.Review;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.req.Review.ReviewRequest;
import com.example.demo.dto.res.Review.ReviewResponse;
import com.example.demo.service.Review.ReviewService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Tạo mới một đánh giá
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ReviewRequest reviewRequest) {
        try {
            ReviewResponse review = reviewService.create(reviewRequest);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Xóa một đánh giá theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            reviewService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Cập nhật thông tin của một đánh giá theo ID
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable int id,
            @RequestBody ReviewRequest reviewRequest) {
        try {
            ReviewResponse review = reviewService.update(id, reviewRequest);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Lấy danh sách tất cả các đánh giá
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<ReviewResponse> review = reviewService.getAll();
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Lấy danh sách các đánh giá theo ID sản phẩm
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getByProductId(@PathVariable int productId) {
        try {
            List<ReviewResponse> review = reviewService.getByProductId(productId);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}