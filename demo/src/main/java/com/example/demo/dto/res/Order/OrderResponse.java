package com.example.demo.dto.res.Order;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.enums.OrderStatus;

import lombok.Data;

@Data
public class OrderResponse {
    private int id;
    private String userFullName;
    private List<OrderItemResponse> orderItem;
    private double total_price;
    private OrderStatus  orderStatus;
    private LocalDateTime create_at;
}
