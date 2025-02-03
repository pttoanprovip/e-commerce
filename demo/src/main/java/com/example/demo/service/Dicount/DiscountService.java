package com.example.demo.service.Dicount;

import java.util.List;

import com.example.demo.dto.req.Discount.ApplyDiscountRequest;
import com.example.demo.dto.req.Discount.DiscountRequest;
import com.example.demo.dto.res.Discount.DiscountResponse;

public interface DiscountService {
    DiscountResponse create(DiscountRequest discountRequest);
    DiscountResponse update(int id, DiscountRequest DiscountRequest);
    void delete(int id);
    List<DiscountResponse> getAll();
    DiscountResponse getById(int id);


    // Áp dụng giảm giá
    double applyDiscount(ApplyDiscountRequest applyDiscountRequest, double totalPrice);
}
