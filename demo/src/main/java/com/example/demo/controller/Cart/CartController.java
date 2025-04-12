package com.example.demo.controller.Cart;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.req.Cart.CartRequest;
import com.example.demo.dto.res.Cart.CartResponse;
import com.example.demo.service.Cart.CartService;

@RestController
@RequestMapping("/carts")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add")
    public ResponseEntity<?> addItem(@RequestBody CartRequest cartRequest) {
        try {
            CartResponse cart = cartService.addItem(cartRequest);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @PostMapping("/remove")
    public ResponseEntity<?> removeItem(@RequestBody CartRequest cartRequest) {
        try {
            CartResponse cart = cartService.removeItem(cartRequest);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Lấy thông tin giỏ hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCart(@PathVariable int id) {
        try {
            CartResponse cart = cartService.getCart(id);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Lấy giỏ hàng theo ID người dùng
    @GetMapping("user/{userId}")
    public ResponseEntity<?> getUserIdCart(@PathVariable int userId) {
        try {
            CartResponse cart = cartService.getUserIdCart(userId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}