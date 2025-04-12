package com.example.demo.service.Impl.PaymentImpl; // Định nghĩa package chứa class này

import java.math.BigDecimal; // Import BigDecimal để xử lý số tiền chính xác
import java.util.Collections; // Import Collections để sử dụng danh sách singleton khi cần

import org.modelmapper.ModelMapper; // Import ModelMapper để ánh xạ giữa DTO và Entity
import org.springframework.security.access.prepost.PreAuthorize; // Import annotation PreAuthorize để kiểm tra quyền trước khi thực thi phương thức
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch

import com.example.demo.dto.req.Payment.PaymentRequest; // Import DTO PaymentRequest để nhận dữ liệu đầu vào
import com.example.demo.dto.res.Payment.PaymentResponse; // Import DTO PaymentResponse để trả về dữ liệu đầu ra
import com.example.demo.entity.Order.Order; // Import lớp Order từ entity
import com.example.demo.entity.Payment.Payment; // Import lớp Payment từ entity
import com.example.demo.enums.PaymentStatus; // Import enum PaymentStatus để quản lý trạng thái thanh toán
import com.example.demo.repository.Order.OrderRepository; // Import OrderRepository để truy vấn Order
import com.example.demo.repository.Payment.PaymentRepository; // Import PaymentRepository để truy vấn Payment
import com.example.demo.service.Payment.PaymentService; // Import interface PaymentService mà lớp này triển khai
import com.paypal.api.payments.Amount; // Import lớp Amount của PayPal để định nghĩa số tiền thanh toán
import com.paypal.api.payments.Payer; // Import lớp Payer của PayPal để định nghĩa thông tin người thanh toán
import com.paypal.api.payments.PaymentExecution; // Import lớp PaymentExecution của PayPal để thực thi thanh toán
import com.paypal.api.payments.RedirectUrls; // Import lớp RedirectUrls của PayPal để định nghĩa URL chuyển hướng
import com.paypal.api.payments.Transaction; // Import lớp Transaction của PayPal để định nghĩa giao dịch
import com.paypal.base.rest.APIContext; // Import lớp APIContext của PayPal để cấu hình API
import com.paypal.base.rest.PayPalRESTException; // Import lớp PayPalRESTException để xử lý lỗi từ PayPal

@Service // Đánh dấu lớp này là một Spring Service
public class PayPalPaymentServiceImpl implements PaymentService { // Lớp triển khai interface PaymentService

    private final PaymentRepository paymentRepository; // Khai báo biến PaymentRepository để tương tác với cơ sở dữ liệu
                                                       // Payment
    private final ModelMapper modelMapper; // Khai báo biến ModelMapper để ánh xạ đối tượng
    private final APIContext apiContext; // Khai báo biến APIContext để cấu hình API PayPal
    private final OrderRepository orderRepository; // Khai báo biến OrderRepository để tương tác với cơ sở dữ liệu Order

    private static final double EXCHANGE_RATE = 24000; // Tỷ giá hối đoái cố định: 1 USD = 24,000 VND

    // Constructor để tiêm các dependency
    public PayPalPaymentServiceImpl(PaymentRepository paymentRepository, ModelMapper modelMapper,
            APIContext apiContext, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository; // Gán PaymentRepository được tiêm vào biến instance
        this.modelMapper = modelMapper; // Gán ModelMapper được tiêm vào biến instance
        this.apiContext = apiContext; // Gán APIContext được tiêm vào biến instance
        this.orderRepository = orderRepository; // Gán OrderRepository được tiêm vào biến instance
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('User')") // Kiểm tra quyền: chỉ User được tạo thanh toán
    public String create(PaymentRequest paymentRequest) { // Phương thức tạo thanh toán mới
        Order order = orderRepository.findById(paymentRequest.getOrderId()) // Tìm Order theo orderId từ request
                .orElseThrow(() -> new RuntimeException("Order not found")); // Ném ngoại lệ nếu không tìm thấy đơn hàng

        Payment payment = new Payment(); // Tạo đối tượng Payment mới
        payment.setOrder(order); // Liên kết thanh toán với đơn hàng
        payment.setPaymentMethod(paymentRequest.getPaymentMethod()); // Gán phương thức thanh toán từ request

        // Chuyển đổi total_price từ VND sang USD
        double paymentAmount = order.getTotal_price().intValue() / EXCHANGE_RATE; // Tính số tiền thanh toán bằng USD
        payment.setAmount(BigDecimal.valueOf(paymentAmount)); // Lưu giá trị USD vào cơ sở dữ liệu
        payment.setPaymentStatus(PaymentStatus.PENDING); // Gán trạng thái thanh toán là PENDING

        Payment savedPayment = paymentRepository.save(payment); // Lưu Payment vào cơ sở dữ liệu

        if ("paypal".equalsIgnoreCase(paymentRequest.getPaymentMethod())) { // Kiểm tra nếu phương thức thanh toán là
                                                                            // PayPal
            try {
                Amount amount = new Amount(); // Tạo đối tượng Amount của PayPal
                amount.setCurrency("USD"); // Đặt đơn vị tiền tệ là USD
                amount.setTotal(String.format("%.2f", paymentAmount)); // Định dạng số tiền USD với 2 chữ số thập phân

                Transaction transaction = new Transaction(); // Tạo đối tượng Transaction của PayPal
                transaction.setAmount(amount); // Gán số tiền cho giao dịch
                transaction.setDescription("Payment for Order #" + order.getId()); // Gán mô tả giao dịch với ID đơn
                                                                                   // hàng

                com.paypal.api.payments.Payment payPalPayment = new com.paypal.api.payments.Payment(); // Tạo đối tượng
                                                                                                       // Payment của
                                                                                                       // PayPal
                payPalPayment.setIntent("sale"); // Đặt mục đích thanh toán là bán hàng

                Payer payer = new Payer(); // Tạo đối tượng Payer của PayPal
                payer.setPaymentMethod("paypal"); // Đặt phương thức thanh toán là PayPal
                payPalPayment.setPayer(payer); // Gán thông tin người thanh toán cho PayPal Payment

                RedirectUrls redirectUrls = new RedirectUrls(); // Tạo đối tượng RedirectUrls của PayPal
                redirectUrls.setReturnUrl("http://localhost:3000/payment-success"); // Đặt URL chuyển hướng khi thanh
                                                                                    // toán thành công
                redirectUrls.setCancelUrl("http://localhost:3000/payment-cancel"); // Đặt URL chuyển hướng khi hủy thanh
                                                                                   // toán
                payPalPayment.setRedirectUrls(redirectUrls); // Gán các URL chuyển hướng cho PayPal Payment

                payPalPayment.setTransactions(Collections.singletonList(transaction)); // Gán danh sách giao dịch (chỉ 1
                                                                                       // giao dịch) cho PayPal Payment

                com.paypal.api.payments.Payment createdPayment = payPalPayment.create(apiContext); // Tạo thanh toán
                                                                                                   // trên PayPal

                String approvalUrl = createdPayment.getLinks().stream() // Lấy URL phê duyệt từ các liên kết của thanh
                                                                        // toán
                        .filter(link -> "approval_url".equals(link.getRel())) // Lọc liên kết có rel là approval_url
                        .findFirst() // Lấy liên kết đầu tiên
                        .map(link -> link.getHref()) // Lấy href của liên kết
                        .orElseThrow(() -> new RuntimeException("Approval URL not found")); // Ném ngoại lệ nếu không
                                                                                            // tìm thấy URL

                savedPayment.setTransactionId(createdPayment.getId()); // Gán ID giao dịch PayPal vào Payment
                paymentRepository.save(savedPayment); // Lưu lại Payment với transactionId

                return approvalUrl; // Trả về URL phê duyệt để chuyển hướng người dùng đến PayPal
            } catch (PayPalRESTException e) { // Bắt ngoại lệ nếu có lỗi từ PayPal
                savedPayment.setPaymentStatus(PaymentStatus.FAILED); // Cập nhật trạng thái thanh toán là FAILED
                paymentRepository.save(savedPayment); // Lưu Payment với trạng thái thất bại
                throw new RuntimeException("PayPal payment creation failed", e); // Ném ngoại lệ với thông báo lỗi
            }
        } else {
            // Xử lý các phương thức khác với total_price gốc (VND)
            payment.setAmount(BigDecimal.valueOf(order.getTotal_price().intValue())); // Gán số tiền gốc bằng VND
            paymentRepository.save(payment); // Lưu Payment vào cơ sở dữ liệu
            return "Payment processed for " + paymentRequest.getPaymentMethod(); // Trả về thông báo xử lý thanh toán
                                                                                 // cho phương thức khác
        }
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    public PaymentResponse execute(String paymentId, String payerId) { // Phương thức thực thi thanh toán PayPal
        Payment payment = paymentRepository.findByTransactionId(paymentId) // Tìm Payment theo transactionId
                .orElseThrow(() -> new RuntimeException("Payment not found")); // Ném ngoại lệ nếu không tìm thấy thanh
                                                                               // toán

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) { // Kiểm tra nếu thanh toán đã thành công trước đó
            throw new RuntimeException("Payment has already been completed."); // Ném ngoại lệ nếu thanh toán đã hoàn
                                                                               // tất
        }

        try {
            PaymentExecution paymentExecution = new PaymentExecution(); // Tạo đối tượng PaymentExecution của PayPal
            paymentExecution.setPayerId(payerId); // Gán payerId từ tham số

            com.paypal.api.payments.Payment payPalPayment = new com.paypal.api.payments.Payment(); // Tạo đối tượng
                                                                                                   // Payment của PayPal
            payPalPayment.setId(payment.getTransactionId()); // Gán ID thanh toán từ Payment trong cơ sở dữ liệu

            com.paypal.api.payments.Payment executedPayment = payPalPayment.execute(apiContext, paymentExecution); // Thực
                                                                                                                   // thi
                                                                                                                   // thanh
                                                                                                                   // toán
                                                                                                                   // trên
                                                                                                                   // PayPal

            payment.setPaymentStatus(PaymentStatus.SUCCESS); // Cập nhật trạng thái thanh toán là SUCCESS
            payment.setTransactionId(executedPayment.getId()); // Cập nhật transactionId từ PayPal
            paymentRepository.save(payment); // Lưu Payment đã cập nhật vào cơ sở dữ liệu

            return modelMapper.map(payment, PaymentResponse.class); // Ánh xạ Payment sang PaymentResponse và trả về
        } catch (PayPalRESTException e) { // Bắt ngoại lệ nếu có lỗi từ PayPal
            payment.setPaymentStatus(PaymentStatus.FAILED); // Cập nhật trạng thái thanh toán là FAILED
            paymentRepository.save(payment); // Lưu Payment với trạng thái thất bại
            throw new RuntimeException("PayPal payment execution failed", e); // Ném ngoại lệ với thông báo lỗi
        }
    }
}