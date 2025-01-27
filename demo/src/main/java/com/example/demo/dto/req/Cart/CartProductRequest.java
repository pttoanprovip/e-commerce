package com.example.demo.dto.req.Cart;

import lombok.Data;

@Data
public class CartProductRequest {
    private int productId;
    private Integer quantity;
}
