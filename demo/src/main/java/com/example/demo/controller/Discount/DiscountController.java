package com.example.demo.controller.Discount;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.req.Discount.ApplyDiscountRequest;
import com.example.demo.dto.req.Discount.DiscountRequest;
import com.example.demo.dto.res.Discount.DiscountResponse;
import com.example.demo.service.Dicount.DiscountService;

@RestController
@RequestMapping("/discounts")
public class DiscountController {

    private DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    // Tạo mới một mã giảm giá
    @PostMapping
    public ResponseEntity<?> create(@RequestBody DiscountRequest discountRequest) {
        try {
            DiscountResponse discount = discountService.create(discountRequest);
            return ResponseEntity.ok(discount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Cập nhật thông tin mã giảm giá theo ID
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable int id,
            @RequestBody DiscountRequest discountRequest) {
        try {
            DiscountResponse discount = discountService.update(id, discountRequest);
            return ResponseEntity.ok(discount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Xóa mã giảm giá theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            discountService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Lấy danh sách tất cả mã giảm giá
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<DiscountResponse> discount = discountService.getAll();
            return ResponseEntity.ok(discount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // Lấy thông tin mã giảm giá theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable int id) {
        try {
            DiscountResponse discount = discountService.getById(id);
            return ResponseEntity.ok(discount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // Áp dụng mã giảm giá cho tổng giá
    @PostMapping("/apply_discount")
    public ResponseEntity<?> applyDiscount(
            @RequestBody ApplyDiscountRequest applyDiscountRequest,
            @RequestParam double totalPrice) {
        try {
            double discountedPrice = discountService.applyDiscount(applyDiscountRequest, totalPrice);
            return ResponseEntity.ok(discountedPrice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}