package com.example.demo.service.Impl.DiscountImpl; // Định nghĩa package chứa class này

import java.time.LocalDateTime; // Import LocalDateTime để xử lý thời gian
import java.util.List; // Import lớp List để sử dụng danh sách
import java.util.Optional; // Import Optional để xử lý kết quả truy vấn có thể null
import java.util.stream.Collectors; // Import Collectors để thu thập kết quả từ stream

import org.modelmapper.ModelMapper; // Import ModelMapper để ánh xạ giữa DTO và Entity
import org.springframework.security.access.prepost.PreAuthorize; // Import annotation PreAuthorize để kiểm tra quyền trước khi thực thi phương thức
//import org.springframework.beans.factory.annotation.Autowired; // Dòng này bị comment, không sử dụng Autowired
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch

import com.example.demo.dto.req.Discount.ApplyDiscountRequest; // Import DTO ApplyDiscountRequest để áp dụng mã giảm giá
import com.example.demo.dto.req.Discount.DiscountRequest; // Import DTO DiscountRequest để nhận dữ liệu đầu vào
import com.example.demo.dto.res.Discount.DiscountResponse; // Import DTO DiscountResponse để trả về dữ liệu đầu ra
import com.example.demo.entity.discount.Discount; // Import lớp Discount từ entity
import com.example.demo.repository.Discount.DiscountRepository; // Import DiscountRepository để truy vấn Discount
import com.example.demo.service.Dicount.DiscountService; // Import interface DiscountService mà lớp này triển khai

@Service // Đánh dấu lớp này là một Spring Service
public class DiscountServiceImpl implements DiscountService { // Lớp triển khai interface DiscountService

    private DiscountRepository discountRepository; // Khai báo biến DiscountRepository để tương tác với cơ sở dữ liệu
                                                   // Discount
    private final ModelMapper modelMapper; // Khai báo biến ModelMapper để ánh xạ đối tượng

    // Constructor để tiêm các dependency
    public DiscountServiceImpl(DiscountRepository discountRepository, ModelMapper modelMapper) {
        this.discountRepository = discountRepository; // Gán DiscountRepository được tiêm vào biến instance
        this.modelMapper = modelMapper; // Gán ModelMapper được tiêm vào biến instance
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được tạo mã giảm giá
    public DiscountResponse create(DiscountRequest discountRequest) { // Phương thức tạo mã giảm giá mới
        // Kiểm tra nếu mã giảm giá đã tồn tại
        Optional<Discount> existDis = discountRepository.findByCode(discountRequest.getCode()); // Tìm Discount theo mã
                                                                                                // code

        if (existDis.isPresent()) { // Kiểm tra nếu mã giảm giá đã tồn tại
            throw new RuntimeException("Discount code already exist"); // Ném ngoại lệ nếu mã đã tồn tại
        }

        // Áp dụng các trường dữ liệu từ request vào entity Discount
        Discount discount = modelMapper.map(discountRequest, Discount.class); // Ánh xạ DiscountRequest sang Discount
                                                                              // entity

        // Lưu mã giảm giá vào cơ sở dữ liệu
        discount = discountRepository.save(discount); // Lưu Discount vào cơ sở dữ liệu

        // Trả về DiscountResponse cho người dùng
        return modelMapper.map(discount, DiscountResponse.class); // Ánh xạ Discount đã lưu sang DiscountResponse và trả
                                                                  // về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được cập nhật mã giảm giá
    public DiscountResponse update(int id, DiscountRequest DiscountRequest) { // Phương thức cập nhật mã giảm giá theo
                                                                              // ID
        // Tìm mã giảm giá theo ID
        Discount discount = discountRepository.findById(id) // Tìm Discount theo ID
                .orElseThrow(() -> new RuntimeException("Discount not found")); // Ném ngoại lệ nếu không tìm thấy mã
                                                                                // giảm giá

        // Áp dụng thay đổi từ request vào entity Discount
        modelMapper.map(DiscountRequest, discount); // Ánh xạ thông tin từ DiscountRequest sang Discount hiện tại

        // Lưu lại mã giảm giá đã cập nhật
        Discount saveDiscount = discountRepository.save(discount); // Lưu Discount đã cập nhật vào cơ sở dữ liệu

        // Trả về DiscountResponse đã được cập nhật
        return modelMapper.map(saveDiscount, DiscountResponse.class); // Ánh xạ Discount đã lưu sang DiscountResponse và
                                                                      // trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được xóa mã giảm giá
    public void delete(int id) { // Phương thức xóa mã giảm giá theo ID
        Discount discount = discountRepository.findById(id) // Tìm Discount theo ID
                .orElseThrow(() -> new RuntimeException("Discount not found")); // Ném ngoại lệ nếu không tìm thấy mã
                                                                                // giảm giá
        discountRepository.delete(discount); // Xóa Discount khỏi cơ sở dữ liệu
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được xem danh sách mã giảm giá
    public List<DiscountResponse> getAll() { // Phương thức lấy danh sách tất cả mã giảm giá
        List<Discount> discounts = discountRepository.findAll(); // Lấy tất cả Discount từ cơ sở dữ liệu
        return discounts.stream() // Chuyển danh sách Discount thành stream
                .map(discount -> modelMapper.map(discount, DiscountResponse.class)) // Ánh xạ từng Discount sang
                                                                                    // DiscountResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được xem mã giảm giá theo ID
    public DiscountResponse getById(int id) { // Phương thức lấy mã giảm giá theo ID
        Discount discount = discountRepository.findById(id) // Tìm Discount theo ID
                .orElseThrow(() -> new RuntimeException("Discount not found")); // Ném ngoại lệ nếu không tìm thấy mã
                                                                                // giảm giá
        return modelMapper.map(discount, DiscountResponse.class); // Ánh xạ Discount sang DiscountResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or hasRole('User')") // Kiểm tra quyền: cả Admin và User được áp dụng mã giảm giá
    public double applyDiscount(ApplyDiscountRequest applyDiscountRequest, double totalPrice) { // Phương thức áp dụng
                                                                                                // mã giảm giá
        // Tìm mã giảm giá theo mã code
        Discount discount = discountRepository.findByCode(applyDiscountRequest.getCode()) // Tìm Discount theo mã code
                .orElseThrow(() -> new RuntimeException("Invalid discount code")); // Ném ngoại lệ nếu mã không hợp lệ

        LocalDateTime now = LocalDateTime.now(); // Lấy thời gian hiện tại

        System.out.println("Current time: " + now); // In thời gian hiện tại để debug
        System.out.println("Discount start: " + discount.getStartDate()); // In thời gian bắt đầu giảm giá để debug
        System.out.println("Discount end: " + discount.getEndDate()); // In thời gian kết thúc giảm giá để debug

        // Kiểm tra tính hợp lệ của thời gian giảm giá
        if (now.isBefore(discount.getStartDate()) || now.isAfter(discount.getEndDate())) { // Kiểm tra nếu thời gian
                                                                                           // hiện tại ngoài khoảng hợp
                                                                                           // lệ
            throw new RuntimeException("Discount is not valid at this time"); // Ném ngoại lệ nếu mã không hợp lệ về
                                                                              // thời gian
        }

        // Kiểm tra trạng thái hoạt động của mã giảm giá
        if (!discount.isActive()) { // Kiểm tra nếu mã giảm giá không hoạt động
            throw new RuntimeException("Discount is not active"); // Ném ngoại lệ nếu mã không hoạt động
        }

        // Tính toán số tiền giảm giá
        double discountAmount = totalPrice * (discount.getDiscountPercentage() / 100); // Tính số tiền giảm dựa trên
                                                                                       // phần trăm giảm giá

        // Nếu có giới hạn giảm giá, tính toán lại
        if (discount.getMaxDiscountAmount() > 0 && discountAmount > discount.getMaxDiscountAmount()) { // Kiểm tra nếu
                                                                                                       // số tiền giảm
                                                                                                       // vượt giới hạn
            discountAmount = discount.getMaxDiscountAmount(); // Gán số tiền giảm bằng giới hạn tối đa
        }

        // Đánh dấu mã giảm giá là đã sử dụng
        discount.setUsed(true); // Cập nhật trạng thái đã sử dụng của mã giảm giá

        // Lưu lại
        discountRepository.save(discount); // Lưu Discount đã cập nhật vào cơ sở dữ liệu

        // Trả về tổng giá trị sau khi đã áp dụng giảm giá
        return totalPrice - discountAmount; // Trả về giá sau khi trừ số tiền giảm
    }

}