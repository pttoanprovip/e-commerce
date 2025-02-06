package com.example.demo.dto.res.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.demo.enums.PaymentStatus;

import lombok.Data;

@Data
public class PaymentResponse {
    private int id;
    private int orderId;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private BigDecimal amount;
    private String transactionId;
    private LocalDateTime createAt = LocalDateTime.now();
}
