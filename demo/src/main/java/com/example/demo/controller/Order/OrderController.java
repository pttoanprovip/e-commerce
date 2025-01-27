package com.example.demo.controller.Order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.req.Order.OrderRequest;
import com.example.demo.dto.res.Order.OrderResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.service.Order.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        try {
            OrderResponse order = orderService.placeOrder(orderRequest);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable int id) {
        try {
            OrderResponse order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getByUserId(@PathVariable int userId) {
        try {
            List<OrderResponse> order = orderService.getUserById(userId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("{userId}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable int userId, @RequestParam OrderStatus orderStatus) {
        try {
            orderService.updateOrderStatus(userId, orderStatus);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }
    }
}
