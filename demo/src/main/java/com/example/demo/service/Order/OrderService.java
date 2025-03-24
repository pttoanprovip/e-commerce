package com.example.demo.service.Order;

import java.util.List;

import com.example.demo.dto.req.Order.OrderRequest;
import com.example.demo.dto.res.Order.OrderResponse;
import com.example.demo.enums.OrderStatus;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderById(int id);

    List<OrderResponse> getUserById(int userId);

    void updateOrderStatus(int id, OrderStatus orderStatus);

    List<OrderResponse> getAllOrders();
}
