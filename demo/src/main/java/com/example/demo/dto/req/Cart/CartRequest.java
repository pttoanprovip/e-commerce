package com.example.demo.dto.req.Cart;

import java.util.List;

import lombok.Data;

@Data
public class CartRequest {
    private int userId;
    List<CartProductRequest> cartProduct;
}
