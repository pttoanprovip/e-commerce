package com.example.demo.repository.Review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Review.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review ,Integer>{
    List<Review> findByProductId(int productId);
}
