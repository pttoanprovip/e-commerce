package com.example.demo.service.Impl.ReviewImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.req.Review.ReviewRequest;
import com.example.demo.dto.res.Review.ReviewResponse;
import com.example.demo.entity.Product.Product;
import com.example.demo.entity.Review.Review;
import com.example.demo.entity.User.User;
import com.example.demo.repository.Product.ProductRepository;
import com.example.demo.repository.Review.ReviewRepository;
import com.example.demo.repository.User.UserRepository;
import com.example.demo.service.Review.ReviewService;

@Service
public class ReviewServiceImpl implements ReviewService {
    private ReviewRepository reviewRepository;
    private UserRepository userRepository;
    private ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ModelMapper modelMapper, UserRepository userRepository,
            ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ReviewResponse create(ReviewRequest reviewRequest) {
        User user = userRepository.findById(reviewRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(reviewRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Review review = modelMapper.map(reviewRequest, Review.class);
        review.setUser(user);
        review.setProduct(product);
        review = reviewRepository.save(review);
        return modelMapper.map(review, ReviewResponse.class);
    }

    @Override
    @Transactional
    public void delete(int id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        reviewRepository.delete(review);
    }

    @Override
    @Transactional
    public ReviewResponse update(int id, ReviewRequest reviewRequest) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setRate(reviewRequest.getRate());
        review.setReviewText(reviewRequest.getReviewText());

        review = reviewRepository.save(review);
        return modelMapper.map(review, ReviewResponse.class);
    }

    @Override
    public List<ReviewResponse> getAll() {
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .map(review -> modelMapper.map(review, ReviewResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getByProductId(int productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(review -> modelMapper.map(review, ReviewResponse.class))
                .collect(Collectors.toList());
    }

}
