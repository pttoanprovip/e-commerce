package com.example.demo.controller.Payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.req.Payment.PaymentRequest;
import com.example.demo.dto.res.Payment.PaymentResponse;
import com.example.demo.service.Payment.PaymentService;

@RestController
@RequestMapping("/payments/zalopay")
public class ZaloPayPaymentController {
    private static final Logger logger = LoggerFactory.getLogger(ZaloPayPaymentController.class);
    private final PaymentService paymentService;

    public ZaloPayPaymentController(@Qualifier("zaloPayPaymentServiceImpl") PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody PaymentRequest paymentRequest) {
        logger.info("Nhận yêu cầu tạo thanh toán: {}", paymentRequest);
        try {
            String orderUrl = paymentService.create(paymentRequest);
            logger.info("Tạo thanh toán thành công, URL: {}", orderUrl);
            return ResponseEntity.ok(orderUrl);
        } catch (RuntimeException e) {
            logger.error("Lỗi khi tạo thanh toán: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Lỗi hệ thống khi tạo thanh toán: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/execute")
    public ResponseEntity<?> execute(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("payerId") String payerId) {
        logger.info("Nhận yêu cầu thực thi thanh toán: paymentId={}, payerId={}", paymentId, payerId);
        try {
            PaymentResponse payment = paymentService.execute(paymentId, payerId);
            logger.info("Thực thi thanh toán thành công: {}", payment);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            logger.error("Lỗi khi thực thi thanh toán: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Lỗi hệ thống khi thực thi thanh toán: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}