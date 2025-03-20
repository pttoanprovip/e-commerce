package com.example.demo.service.Impl.PaymentImpl;

import java.math.BigDecimal;
import java.util.Collections;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // Constructor để khởi tạo các dependency
    public PayPalPaymentServiceImpl(PaymentRepository paymentRepository, ModelMapper modelMapper,
            APIContext apiContext, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.apiContext = apiContext;
        this.modelMapper = modelMapper;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('User')")
    public String create(PaymentRequest paymentRequest) {
        // Lấy Order từ cơ sở dữ liệu
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Tạo đối tượng Payment từ thông tin trong paymentRequest
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setAmount(BigDecimal.valueOf(order.getTotal_price()));
        payment.setPaymentStatus(PaymentStatus.PENDING);

        // Lưu Payment vào cơ sở dữ liệu
        Payment savedPayment = paymentRepository.save(payment);

        try {
            // Tạo đối tượng Amount từ tổng giá trị của đơn hàng
            Amount amount = new Amount();
            amount.setCurrency("USD"); // Đơn vị tiền tệ là USD
            amount.setTotal(String.format("%.2f", order.getTotal_price())); // Định dạng giá trị tiền

            // Tạo đối tượng Transaction để mô tả giao dịch
            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setDescription("Payment for Order #" + order.getId());

            // Tạo đối tượng Payment của PayPal
            com.paypal.api.payments.Payment payPalPayment = new com.paypal.api.payments.Payment();
            payPalPayment.setIntent("sale"); // Xác định loại thanh toán là "sale" (bán)

            // Tạo đối tượng Payer và thiết lập phương thức thanh toán
            Payer payer = new Payer();
            payer.setPaymentMethod(paymentRequest.getPaymentMethod());
            payPalPayment.setPayer(payer);

            // Tạo redirect URLs (URL quay lại và hủy bỏ thanh toán)
            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setReturnUrl("http://localhost:8080/payments/execute");
            redirectUrls.setCancelUrl("http://localhost:8080/payments/cancel");
            payPalPayment.setRedirectUrls(redirectUrls);

            // Thêm giao dịch vào đối tượng Payment của PayPal
            payPalPayment.setTransactions(Collections.singletonList(transaction));

            // Thực hiện thanh toán qua PayPal
            com.paypal.api.payments.Payment createPayment = payPalPayment.create(apiContext);

            // Lấy link thanh toán (approval URL) từ PayPal
            String approvalUrl = createPayment.getLinks().stream()
                    .filter(link -> "approval_url".equals(link.getRel())) // Lọc để lấy approval URL
                    .findFirst()
                    .map(link -> link.getHref())
                    .orElseThrow(() -> new RuntimeException("Approval URL not found"));

            // Lưu transactionId tạm thời vào Payment trong cơ sở dữ liệu
            savedPayment.setTransactionId(createPayment.getId());
            paymentRepository.save(savedPayment);

            return approvalUrl; // Trả về URL chấp nhận thanh toán
        } catch (PayPalRESTException e) {
            // Nếu có lỗi khi tạo thanh toán, cập nhật trạng thái thanh toán là FAILED
            savedPayment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(savedPayment);
            throw new RuntimeException("PayPal payment creation failed", e);
        }
    }

    @Override
    @Transactional
    public PaymentResponse execute(String paymentId, String payerId) {
        // Lấy Payment từ cơ sở dữ liệu bằng transactionId (paymentId)
        Payment payment = paymentRepository.findByTransactionId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Kiểm tra nếu thanh toán đã thành công thì không xử lý lại
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment has already been completed.");
        }

        try {
            // Thiết lập PaymentExecution với payerId từ PayPal
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            // Tạo đối tượng Payment của PayPal từ transactionId
            com.paypal.api.payments.Payment payPalPayment = new com.paypal.api.payments.Payment();
            payPalPayment.setId(payment.getTransactionId());

            // Thực hiện thanh toán qua PayPal
            com.paypal.api.payments.Payment executedPayment = payPalPayment.execute(apiContext, paymentExecution);

            // Cập nhật trạng thái thanh toán thành SUCCESS và lưu lại thông tin
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(executedPayment.getId());
            paymentRepository.save(payment);

            // Trả về PaymentResponse đã ánh xạ từ Payment
            return modelMapper.map(payment, PaymentResponse.class);
        } catch (PayPalRESTException e) {
            // Nếu có lỗi khi thực hiện thanh toán, cập nhật trạng thái thanh toán là FAILED
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("PayPal payment execution failed", e);
        }
    }

}
