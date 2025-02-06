package com.example.demo.dto.req.Payment;
        

import lombok.Data;

@Data
public class PaymentRequest {
    private int orderId;
    private String paymentMethod;
}
