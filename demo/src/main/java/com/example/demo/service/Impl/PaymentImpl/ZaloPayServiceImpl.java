package com.example.demo.service.Impl.PaymentImpl;

import com.example.demo.config.ZalopayConfig;
import com.example.demo.dto.req.Payment.PaymentRequest;
import com.example.demo.dto.res.Payment.PaymentResponse;
import com.example.demo.entity.Order.Order;
import com.example.demo.entity.Payment.Payment;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repository.Order.OrderRepository;
import com.example.demo.repository.Payment.PaymentRepository;
import com.example.demo.service.Payment.PaymentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("zaloPayPaymentServiceImpl")
public class ZaloPayServiceImpl implements PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(ZaloPayServiceImpl.class);

    private final ZalopayConfig config;
    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public ZaloPayServiceImpl(ZalopayConfig config, RestTemplate restTemplate,
            OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    private String hmacSHA256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return org.apache.commons.codec.binary.Hex
                    .encodeHexString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            logger.error("Error generating HMAC: ", e);
            throw new RuntimeException("Could not generate HMAC", e);
        }
    }

    private String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('User')")
    public String create(PaymentRequest paymentRequest) {
        logger.info("Starting payment creation for request={}", paymentRequest);

        // Fetch order and validate
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Create payment record
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setAmount(order.getTotal_price());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        // Generate ZaloPay request parameters
        String appTransId = getCurrentTimeString("yyMMdd") + "_" + new Random().nextInt(1000000);
        long appTime = System.currentTimeMillis();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("app_id", config.getAppId());
        payload.put("app_trans_id", appTransId);
        payload.put("app_user", "user" + order.getId());
        payload.put("app_time", appTime);
        payload.put("amount", order.getTotal_price().longValue());
        payload.put("embed_data", "{}");
        payload.put("item", "[{}]");
        payload.put("description", "Thanh toán cho đơn hàng #" + order.getId());
        payload.put("bank_code", "");
        payload.put("callback_url", "http://localhost:8080/payments/zalopay/callback");

        // Generate MAC
        String macData = config.getAppId() + "|" + appTransId + "|" + "user" + order.getId() + "|"
                + order.getTotal_price().longValue() + "|" + appTime + "|" + "{}" + "|" + "[{}]";
        logger.info("MAC data: {}", macData);
        String mac = hmacSHA256(macData, config.getKey1());
        payload.put("mac", mac);

        logger.info("Sending request to ZaloPay: {}", payload);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getEndpoint() + "/v2/create",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class);

            Map<String, Object> responseBody = response.getBody();
            logger.info("Response from ZaloPay: {}", responseBody);

            if (responseBody != null && responseBody.containsKey("return_code") &&
                    "1".equals(String.valueOf(responseBody.get("return_code")))) {
                payment.setTransactionId(appTransId);
                payment.setPaymentStatus(PaymentStatus.PENDING);
                paymentRepository.save(payment);
                return (String) responseBody.get("order_url");
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                String errorMessage = responseBody != null ? responseBody.toString() : "No response body";
                throw new RuntimeException("ZaloPay payment creation failed: " + errorMessage);
            }
        } catch (Exception e) {
            logger.error("Error during ZaloPay payment creation: ", e);
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResponse execute(String paymentId, String payerId) {
        throw new UnsupportedOperationException("Payment execution not implemented yet");
    }
}