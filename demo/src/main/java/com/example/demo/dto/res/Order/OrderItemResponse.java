package com.example.demo.dto.res.Order;

import lombok.Data;

@Data
public class OrderItemResponse {
    private int id;
    private String productName;
    private int quantity;
    private double price;
}
