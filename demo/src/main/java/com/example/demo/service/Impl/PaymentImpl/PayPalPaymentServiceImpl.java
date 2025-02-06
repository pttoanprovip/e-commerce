package com.example.demo.service.Impl.PaymentImpl;

import java.math.BigDecimal;
import java.util.Collections;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.req.Payment.PaymentRequest;
import com.example.demo.dto.res.Payment.PaymentResponse;
import com.example.demo.entity.Order.Order;
import com.example.demo.entity.Payment.Payment;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repository.Order.OrderRepository;
import com.example.demo.repository.Payment.PaymentRepository;
import com.example.demo.service.Payment.PaymentService;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

@Service
public class PayPalPaymentServiceImpl implements PaymentService {

    private PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    private final APIContext apiContext;
    private OrderRepository orderRepository;

    public PayPalPaymentServiceImpl(PaymentRepository paymentRepository, ModelMapper modelMapper,
            APIContext apiContext, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.apiContext = apiContext;
        this.modelMapper = modelMapper;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public String create(PaymentRequest paymentRequest) {
        // Lấy Order từ cơ sở dữ liệu
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Tạo đối tượng Payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setAmount(BigDecimal.valueOf(order.getTotal_price()));
        payment.setPaymentStatus(PaymentStatus.PENDING);

        // Lưu Payment vào cơ sở dữ liệu
        Payment savedPayment = paymentRepository.save(payment);

        try {
            // Tạo cấu hình thông tin thanh toán
            Amount amount = new Amount();
            amount.setCurrency("USD");
            // Sử dụng tổng giá trị từ Order
            amount.setTotal(String.format("%.2f", order.getTotal_price()));

            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setDescription("Payment for Order #" + order.getId());

            // Tiến hành tạo thanh toán qua PayPal
            // PayPalPayment payPalPayment = new PayPalPayment();
            com.paypal.api.payments.Payment payPalPayment = new com.paypal.api.payments.Payment();
            payPalPayment.setIntent("sale");

            // Tạo đối tượng payer và thiết lập thông tin
            Payer payer = new Payer();
            payer.setPaymentMethod(paymentRequest.getPaymentMethod());
            payPalPayment.setPayer(payer);

            // Tạo redirect URLs
            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setReturnUrl("http://localhost:8080/payments/execute");
            redirectUrls.setCancelUrl("http://localhost:8080/payments/cancel");
            payPalPayment.setRedirectUrls(redirectUrls);

            // Thêm transaction vào payment
            payPalPayment.setTransactions(Collections.singletonList(transaction));

            // Thực hiện thanh toán
            com.paypal.api.payments.Payment createPayment = payPalPayment.create(apiContext);

            // Lấy link thanh toán từ PayPal
            String approvalUrl = createPayment.getLinks().stream()
                    .filter(link -> "approval_url".equals(link.getRel()))
                    .findFirst()
                    .map(link -> link.getHref())
                    .orElseThrow(() -> new RuntimeException("Approval URL not found"));

            // Lưu transactionId tạm thời
            savedPayment.setTransactionId(createPayment.getId());
            paymentRepository.save(savedPayment);

            return approvalUrl;
        } catch (PayPalRESTException e) {
            savedPayment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(savedPayment);
            throw new RuntimeException("PayPal payment creation failed", e);
        }

    }

    @Override
    @Transactional
    public PaymentResponse execute(String paymentId, String payerId) {
        Payment payment = paymentRepository.findByTransactionId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Kiểm tra xem thanh toán đã được thực hiện thành công chưa
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment has already been completed.");
        }

        try {
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            com.paypal.api.payments.Payment payPalPayment = new com.paypal.api.payments.Payment();
            payPalPayment.setId(payment.getTransactionId());

            // Thực hiện thanh toán
            com.paypal.api.payments.Payment executedPayment = payPalPayment.execute(apiContext, paymentExecution);

            // Cập nhật trạng thái thanh toán
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(executedPayment.getId());
            paymentRepository.save(payment);

            return modelMapper.map(payment, PaymentResponse.class);
        } catch (PayPalRESTException e) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("PayPal payment execution failed", e);
        }
    }

}
