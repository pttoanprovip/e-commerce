package com.example.demo.service.Impl.DiscountImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.req.Discount.ApplyDiscountRequest;
import com.example.demo.dto.req.Discount.DiscountRequest;
import com.example.demo.dto.res.Discount.DiscountResponse;
import com.example.demo.entity.discount.Discount;
import com.example.demo.repository.Discount.DiscountRepository;
import com.example.demo.service.Dicount.DiscountService;

@Service
public class DiscountServiceImpl implements DiscountService {

    private DiscountRepository discountRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public DiscountServiceImpl(DiscountRepository discountRepository, ModelMapper modelMapper) {
        this.discountRepository = discountRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public DiscountResponse create(DiscountRequest discountRequest) {
        // Kiểm tra nếu mã giảm giá đã tồn tại
        Optional<Discount> existDis = discountRepository.findByCode(discountRequest.getCode());

        if (existDis.isPresent()) {
            throw new RuntimeException("Discount code already exist");
        }

        // Áp dụng các trường dữ liệu từ request vào entity Discount
        Discount discount = modelMapper.map(discountRequest, Discount.class);

        // Lưu mã giảm giá vào cơ sở dữ liệu
        discount = discountRepository.save(discount);

        // Trả về DiscountResponse cho người dùng
        return modelMapper.map(discount, DiscountResponse.class);
    }

    @Override
    @Transactional
    public DiscountResponse update(int id, DiscountRequest DiscountRequest) {
        // Tìm mã giảm giá theo ID
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        // Áp dụng thay đổi từ request vào entity Discount
        modelMapper.map(DiscountRequest, discount);

        // Lưu lại mã giảm giá đã cập nhật
        Discount saveDiscount = discountRepository.save(discount);

        // Trả về DiscountResponse đã được cập nhật
        return modelMapper.map(saveDiscount, DiscountResponse.class);
    }

    @Override
    @Transactional
    public void delete(int id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        discountRepository.delete(discount);
    }

    @Override
    public List<DiscountResponse> getAll() {
        List<Discount> discounts = discountRepository.findAll();
        return discounts.stream()
                .map(discount -> modelMapper.map(discount, DiscountResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public DiscountResponse getById(int id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        return modelMapper.map(discount, DiscountResponse.class);
    }

    @Override
    public double applyDiscount(ApplyDiscountRequest applyDiscountRequest, double totalPrice) {
        // Tìm mã giảm giá theo mã code
        Discount discount = discountRepository.findByCode(applyDiscountRequest.getCode())
                .orElseThrow(() -> new RuntimeException("Invalid discount code"));

        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra tính hợp lệ của thời gian giảm giá
        if (now.isBefore(discount.getStartDate()) || now.isAfter(discount.getEndDate())) {
            throw new RuntimeException("Discount is not valid at this time");
        }

        // Kiểm tra trạng thái hoạt động của mã giảm giá
        if (!discount.isActive()) {
            throw new RuntimeException("Discount is not active");
        }

        // Tính toán số tiền giảm giá
        double discountAmount = totalPrice * (discount.getDiscountPercentage() / 100);

        // Nếu có giới hạn giảm giá, tính toán lại
        if(discount.getMaxDiscountAmount() > 0 && discountAmount > discount.getMaxDiscountAmount()){
            discountAmount = discount.getMaxDiscountAmount();
        }

         // Đánh dấu mã giảm giá là đã sử dụng
        discount.setUsed(true);

        // Lưu lại
        discountRepository.save(discount);

        // Trả về tổng giá trị sau khi đã áp dụng giảm giá
        return totalPrice - discountAmount;
    }

}
