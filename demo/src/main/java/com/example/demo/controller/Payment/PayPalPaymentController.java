package com.example.demo.controller.Payment;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.req.Payment.PaymentRequest;
import com.example.demo.dto.res.Payment.PaymentResponse;
import com.example.demo.service.Payment.PaymentService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/payments/paypal")
public class PayPalPaymentController {

    private PaymentService paymentService;

    public PayPalPaymentController(@Qualifier("payPalPaymentServiceImpl") PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Tạo thanh toán PayPal
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody PaymentRequest paymentRequest) {
        try {
            String approvalURL = paymentService.create(paymentRequest);
            return ResponseEntity.ok(approvalURL);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Thực thi thanh toán PayPal
    @GetMapping("/execute")
    public ResponseEntity<?> execute(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId) {
        try {
            PaymentResponse payment = paymentService.execute(paymentId, payerId);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }
}