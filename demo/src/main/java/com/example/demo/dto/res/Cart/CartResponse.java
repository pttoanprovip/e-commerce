package com.example.demo.dto.res.Cart;

import java.util.List;

import lombok.Data;

@Data
public class CartResponse {
    private int id;
    private List<CartProductResponse> cartProduct;
    private double total_price;
}
