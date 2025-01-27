package com.example.demo.dto.req.Order;

import lombok.Data;

@Data
public class OrderItemRequest {
    private int productId;
    private int quantity;
}
