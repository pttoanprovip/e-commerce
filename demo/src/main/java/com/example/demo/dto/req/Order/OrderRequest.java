package com.example.demo.dto.req.Order;

import java.util.List;

import lombok.Data;
@Data
public class OrderRequest {
    private int userId;
    private List<OrderItemRequest> order_items;
}
