package com.example.demo.dto.res.Cart;

import lombok.Data;

@Data
public class CartProductResponse {
    private int id;
    private String productName;
    private int quantity;
    private double price;
}
