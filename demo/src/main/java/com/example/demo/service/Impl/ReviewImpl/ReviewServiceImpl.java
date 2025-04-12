package com.example.demo.service.Impl.ReviewImpl; // Định nghĩa package chứa class này

import java.util.List; // Import lớp List để sử dụng danh sách
import java.util.stream.Collectors; // Import Collectors để thu thập kết quả từ stream

import org.modelmapper.ModelMapper; // Import ModelMapper để ánh xạ giữa DTO và Entity
import org.springframework.security.access.prepost.PreAuthorize; // Import annotation PreAuthorize để kiểm tra quyền trước khi thực thi phương thức
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch

import com.example.demo.dto.req.Review.ReviewRequest; // Import DTO ReviewRequest để nhận dữ liệu đầu vào
import com.example.demo.dto.res.Review.ReviewResponse; // Import DTO ReviewResponse để trả về dữ liệu đầu ra
import com.example.demo.entity.Product.Product; // Import lớp Product từ entity
import com.example.demo.entity.Review.Review; // Import lớp Review từ entity
import com.example.demo.entity.User.User; // Import lớp User từ entity
import com.example.demo.repository.Product.ProductRepository; // Import ProductRepository để truy vấn Product
import com.example.demo.repository.Review.ReviewRepository; // Import ReviewRepository để truy vấn Review
import com.example.demo.repository.User.UserRepository; // Import UserRepository để truy vấn User
import com.example.demo.service.Review.ReviewService; // Import interface ReviewService mà lớp này triển khai

@Service // Đánh dấu lớp này là một Spring Service
public class ReviewServiceImpl implements ReviewService { // Lớp triển khai interface ReviewService

    private ReviewRepository reviewRepository; // Khai báo biến ReviewRepository để tương tác với cơ sở dữ liệu Review
    private UserRepository userRepository; // Khai báo biến UserRepository để tương tác với cơ sở dữ liệu User
    private ProductRepository productRepository; // Khai báo biến ProductRepository để tương tác với cơ sở dữ liệu
                                                 // Product
    private final ModelMapper modelMapper; // Khai báo biến ModelMapper để ánh xạ đối tượng

    // Constructor để tiêm các dependency
    public ReviewServiceImpl(ReviewRepository reviewRepository, ModelMapper modelMapper, UserRepository userRepository,
            ProductRepository productRepository) {
        this.reviewRepository = reviewRepository; // Gán ReviewRepository được tiêm vào biến instance
        this.modelMapper = modelMapper; // Gán ModelMapper được tiêm vào biến instance
        this.userRepository = userRepository; // Gán UserRepository được tiêm vào biến instance
        this.productRepository = productRepository; // Gán ProductRepository được tiêm vào biến instance
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch, tự động quản lý rollback nếu có
                   // lỗi
    public ReviewResponse create(ReviewRequest reviewRequest) { // Phương thức tạo đánh giá mới

        User user = userRepository.findById(reviewRequest.getUserId()) // Tìm User theo userId từ request
                .orElseThrow(() -> new RuntimeException("User not found")); // Ném ngoại lệ nếu không tìm thấy user

        Product product = productRepository.findById(reviewRequest.getProductId()) // Tìm Product theo productId từ
                                                                                   // request
                .orElseThrow(() -> new RuntimeException("Product not found")); // Ném ngoại lệ nếu không tìm thấy
                                                                               // product

        Review review = modelMapper.map(reviewRequest, Review.class); // Ánh xạ ReviewRequest sang Review entity
        review.setUser(user); // Liên kết đánh giá với đối tượng User
        review.setProduct(product); // Liên kết đánh giá với đối tượng Product
        review = reviewRepository.save(review); // Lưu đối tượng Review vào cơ sở dữ liệu

        return modelMapper.map(review, ReviewResponse.class); // Ánh xạ Review đã lưu sang ReviewResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(#reviewRequest.userId) == authentication.principal.claims['sub']") // Kiểm
                                                                                                                            // tra
                                                                                                                            // quyền:
                                                                                                                            // chỉ
                                                                                                                            // Admin
                                                                                                                            // hoặc
                                                                                                                            // người
                                                                                                                            // dùng
                                                                                                                            // sở
                                                                                                                            // hữu
                                                                                                                            // userId
                                                                                                                            // được
                                                                                                                            // xóa
                                                                                                                            // đánh
                                                                                                                            // giá
    public void delete(int id) { // Phương thức xóa đánh giá theo ID

        Review review = reviewRepository.findById(id) // Tìm Review theo ID
                .orElseThrow(() -> new RuntimeException("Review not found")); // Ném ngoại lệ nếu không tìm thấy đánh
                                                                              // giá

        reviewRepository.delete(review); // Xóa đối tượng Review khỏi cơ sở dữ liệu
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(#reviewRequest.userId) == authentication.principal.claims['sub']") // Kiểm
                                                                                                                            // tra
                                                                                                                            // quyền:
                                                                                                                            // chỉ
                                                                                                                            // Admin
                                                                                                                            // hoặc
                                                                                                                            // người
                                                                                                                            // dùng
                                                                                                                            // sở
                                                                                                                            // hữu
                                                                                                                            // userId
                                                                                                                            // được
                                                                                                                            // cập
                                                                                                                            // nhật
                                                                                                                            // đánh
                                                                                                                            // giá
    public ReviewResponse update(int id, ReviewRequest reviewRequest) { // Phương thức cập nhật đánh giá theo ID

        Review review = reviewRepository.findById(id) // Tìm Review theo ID
                .orElseThrow(() -> new RuntimeException("Review not found")); // Ném ngoại lệ nếu không tìm thấy đánh
                                                                              // giá

        review.setRate(reviewRequest.getRate()); // Cập nhật điểm đánh giá từ request
        review.setReviewText(reviewRequest.getReviewText()); // Cập nhật nội dung đánh giá từ request

        review = reviewRepository.save(review); // Lưu đối tượng Review đã cập nhật vào cơ sở dữ liệu
        return modelMapper.map(review, ReviewResponse.class); // Ánh xạ Review đã lưu sang ReviewResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ReviewResponse> getAll() { // Phương thức lấy danh sách tất cả đánh giá

        List<Review> reviews = reviewRepository.findAll(); // Lấy tất cả Review từ cơ sở dữ liệu
        return reviews.stream() // Chuyển danh sách Review thành stream
                .map(review -> modelMapper.map(review, ReviewResponse.class)) // Ánh xạ từng Review sang ReviewResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ReviewResponse> getByProductId(int productId) { // Phương thức lấy danh sách đánh giá theo productId

        List<Review> reviews = reviewRepository.findByProductId(productId); // Tìm danh sách Review theo productId
        return reviews.stream() // Chuyển danh sách Review thành stream
                .map(review -> modelMapper.map(review, ReviewResponse.class)) // Ánh xạ từng Review sang ReviewResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

}