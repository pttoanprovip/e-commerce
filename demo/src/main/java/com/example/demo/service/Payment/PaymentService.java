package com.example.demo.service.Payment;

import com.example.demo.dto.req.Payment.PaymentRequest;
import com.example.demo.dto.res.Payment.PaymentResponse;

public interface PaymentService {
    String create(PaymentRequest paymentRequest);
    PaymentResponse execute(String paymentId, String payerId);
}
    