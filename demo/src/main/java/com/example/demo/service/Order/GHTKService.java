package com.example.demo.service.Order; // Định nghĩa package chứa class này

import java.math.BigDecimal; // Import BigDecimal để xử lý số tiền chính xác
import java.util.HashMap; // Import HashMap để tạo cấu trúc dữ liệu dạng key-value
import java.util.List; // Import List để sử dụng danh sách
import java.util.Map; // Import Map để sử dụng cấu trúc key-value
import java.util.stream.Collectors; // Import Collectors để thu thập kết quả từ stream

import org.springframework.beans.factory.annotation.Value; // Import annotation Value để tiêm giá trị từ cấu hình
import org.springframework.http.HttpEntity; // Import HttpEntity để gửi yêu cầu HTTP
import org.springframework.http.HttpHeaders; // Import HttpHeaders để thiết lập header cho yêu cầu HTTP
import org.springframework.http.HttpMethod; // Import HttpMethod để xác định phương thức HTTP
import org.springframework.http.ResponseEntity; // Import ResponseEntity để nhận phản hồi từ API
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.web.client.RestTemplate; // Import RestTemplate để thực hiện các yêu cầu HTTP

import com.example.demo.entity.Order.Order; // Import lớp Order từ entity
import com.example.demo.entity.Order.OrderItem; // Import lớp OrderItem từ entity
import com.example.demo.entity.User.UserAddress; // Import lớp UserAddress từ entity
import com.example.demo.enums.OrderStatus; // Import enum OrderStatus để quản lý trạng thái đơn hàng

@Service // Đánh dấu lớp này là một Spring Service
public class GHTKService { // Lớp cung cấp các dịch vụ liên quan đến Giao Hàng Tiết Kiệm (GHTK)
    private final RestTemplate restTemplate; // Khai báo biến RestTemplate để gửi yêu cầu HTTP

    @Value("${ghtk.api.token}") // Tiêm giá trị token API của GHTK từ file cấu hình
    private String ghtkToken; // Khai báo biến lưu token API GHTK

    @Value("${ghtk.api.base-url}") // Tiêm giá trị URL cơ sở của API GHTK từ file cấu hình
    private String ghtkUrl; // Khai báo biến lưu URL cơ sở của GHTK

    @Value("${ghtk.seller.phone}") // Tiêm số điện thoại của người gửi từ file cấu hình
    private String sellerPhone; // Khai báo biến lưu số điện thoại người gửi

    // Constructor để tiêm dependency
    public GHTKService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate; // Gán RestTemplate được tiêm vào biến instance
    }

    // Tính phí ship
    public BigDecimal calculateShippingFee(String pickProvince, String pickDistrict, String province, String district,
            int weight) { // Phương thức tính phí vận chuyển
        String url = ghtkUrl + "/services/shipment/fee"; // Tạo URL API để tính phí vận chuyển

        HttpHeaders headers = new HttpHeaders(); // Tạo đối tượng HttpHeaders
        headers.set("Token", ghtkToken); // Đặt token GHTK vào header
        headers.set("Content-Type", "application/json"); // Đặt loại nội dung là JSON

        Map<String, Object> requestBody = new HashMap<>(); // Tạo body cho yêu cầu
        requestBody.put("pick_province", pickProvince); // Đặt tỉnh/thành phố lấy hàng
        requestBody.put("pick_district", pickDistrict); // Đặt quận/huyện lấy hàng
        requestBody.put("province", province); // Đặt tỉnh/thành phố giao hàng
        requestBody.put("district", district); // Đặt quận/huyện giao hàng
        requestBody.put("weight", weight); // Đặt trọng lượng (đơn vị: gram)

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers); // Tạo HttpEntity với body và
                                                                                         // header

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class); // Gửi yêu
                                                                                                           // cầu POST
                                                                                                           // và nhận
                                                                                                           // phản hồi
            Map<String, Object> responseBody = response.getBody(); // Lấy body từ phản hồi

            if (responseBody != null && responseBody.containsKey("success") && (boolean) responseBody.get("success")) { // Kiểm
                                                                                                                        // tra
                                                                                                                        // nếu
                                                                                                                        // yêu
                                                                                                                        // cầu
                                                                                                                        // thành
                                                                                                                        // công
                Map<String, Object> fee = (Map<String, Object>) responseBody.get("fee"); // Lấy thông tin phí từ phản
                                                                                         // hồi
                return BigDecimal.valueOf((Integer) fee.get("fee")); // Trả về phí vận chuyển dưới dạng BigDecimal
            } else { // Nếu yêu cầu thất bại
                System.err.println("Shipping fee calculation failed: "
                        + (responseBody != null ? responseBody.get("message") : "No response")); // In lỗi ra console
                return BigDecimal.valueOf(30000); // Trả về phí mặc định 30,000 VND
            }
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi khi gửi yêu cầu
            System.err.println("Error calculating shipping fee: " + e.getMessage()); // In lỗi ra console
            return BigDecimal.valueOf(30000); // Trả về phí mặc định 30,000 VND khi có lỗi
        }
    }

    // Lấy trạng thái đơn hàng từ GHTK
    public OrderStatus getOrderStatusFromGHTK(String ghtkOrderCode) { // Phương thức lấy trạng thái đơn hàng từ GHTK
        String url = ghtkUrl + "/services/shipment/v2/" + ghtkOrderCode; // Tạo URL API để lấy trạng thái đơn hàng

        HttpHeaders headers = new HttpHeaders(); // Tạo đối tượng HttpHeaders
        headers.set("Token", ghtkToken); // Đặt token GHTK vào header

        HttpEntity<String> entity = new HttpEntity<>(headers); // Tạo HttpEntity với header (không cần body)

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class); // Gửi yêu cầu
                                                                                                          // GET và nhận
                                                                                                          // phản hồi
            Map<String, Object> responseBody = response.getBody(); // Lấy body từ phản hồi

            if (responseBody != null && responseBody.containsKey("success") && (boolean) responseBody.get("success")) { // Kiểm
                                                                                                                        // tra
                                                                                                                        // nếu
                                                                                                                        // yêu
                                                                                                                        // cầu
                                                                                                                        // thành
                                                                                                                        // công
                String status = (String) responseBody.get("status"); // Lấy trạng thái từ phản hồi
                return mapGHTKStatusToOrderStatus(status); // Ánh xạ trạng thái GHTK sang OrderStatus
            } else { // Nếu yêu cầu thất bại
                System.err.println("Could not get order status from GHTK: "
                        + (responseBody != null ? responseBody.get("message") : "No response")); // In lỗi ra console
                return OrderStatus.Pending; // Trả về trạng thái mặc định PENDING
            }
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi khi gửi yêu cầu
            System.err.println("Error getting order status: " + e.getMessage()); // In lỗi ra console
            return OrderStatus.Pending; // Trả về trạng thái mặc định PENDING khi có lỗi
        }
    }

    // Ánh xạ trạng thái GHTK sang OrderStatus
    private OrderStatus mapGHTKStatusToOrderStatus(String ghtkStatus) { // Phương thức ánh xạ trạng thái GHTK sang
                                                                        // OrderStatus
        switch (ghtkStatus) { // Kiểm tra giá trị trạng thái GHTK
            case "-1": // Chưa nhận hàng
            case "1": // Đã nhận hàng từ người gửi
                return OrderStatus.Pending; // Ánh xạ sang trạng thái PENDING
            case "2": // Đang xử lý
            case "3": // Đang giao hàng
                return OrderStatus.Processing; // Ánh xạ sang trạng thái PROCESSING
            case "4": // Đã giao hàng
                return OrderStatus.Shipped; // Ánh xạ sang trạng thái SHIPPED
            case "5": // Đã giao hàng thành công
                return OrderStatus.Delivered; // Ánh xạ sang trạng thái DELIVERED
            case "6": // Đơn hàng bị hủy
                return OrderStatus.Cancelled; // Ánh xạ sang trạng thái CANCELLED
            default: // Trường hợp không xác định
                return OrderStatus.Pending; // Trả về trạng thái mặc định PENDING
        }
    }

    public String createOrderWithGHTK(Order order, List<OrderItem> orderItems, UserAddress userAddress,
            BigDecimal productTotal) { // Phương thức tạo đơn hàng với GHTK
        Map<String, Object> requestBody = new HashMap<>(); // Tạo body cho yêu cầu
        Map<String, Object> orderDetails = new HashMap<>(); // Tạo chi tiết đơn hàng
        orderDetails.put("id", "ORDER_" + order.getId()); // Đặt ID đơn hàng với tiền tố "ORDER_"
        orderDetails.put("pick_name", "SellPhoneT"); // Đặt tên người gửi
        orderDetails.put("pick_address", "123 Nguyễn Văn Cừ, Phường 4, Quận 7"); // Đặt địa chỉ lấy hàng
        orderDetails.put("pick_province", "Hồ Chí Minh"); // Đặt tỉnh/thành phố lấy hàng
        orderDetails.put("pick_district", "Quận 7"); // Đặt quận/huyện lấy hàng
        orderDetails.put("pick_ward", "Phường 4"); // Đặt phường/xã lấy hàng
        orderDetails.put("pick_tel", sellerPhone); // Đặt số điện thoại người gửi

        // Recipient details
        orderDetails.put("name", userAddress.getUser().getName()); // Đặt tên người nhận từ UserAddress

        // Use userAddress fields directly
        String street = userAddress.getAddress() != null ? userAddress.getAddress().trim() : ""; // Lấy và chuẩn hóa địa
                                                                                                 // chỉ đường
        String district = userAddress.getDistrict() != null ? userAddress.getDistrict().trim() : ""; // Lấy và chuẩn hóa
                                                                                                     // quận/huyện
        String ward = userAddress.getWard() != null ? userAddress.getWard().trim() : ""; // Lấy và chuẩn hóa phường/xã
        String city = userAddress.getCity() != null
                ? userAddress.getCity().replaceAll("^(TP\\.|Thành phố\\s+)", "").trim()
                : ""; // Lấy và chuẩn hóa thành phố

        // Validate district
        if (district.isEmpty()) { // Kiểm tra nếu quận/huyện trống
            System.out.println("Error: District is missing for user address ID: " + userAddress.getId()); // In lỗi
            return null; // Trả về null nếu thiếu quận/huyện
        }

        orderDetails.put("address", street); // Đặt địa chỉ đường
        orderDetails.put("province", city); // Đặt tỉnh/thành phố giao hàng
        orderDetails.put("district", district); // Đặt quận/huyện giao hàng
        orderDetails.put("ward", ward); // Đặt phường/xã giao hàng
        orderDetails.put("hamlet", "Khác"); // Đặt thôn/xóm mặc định là "Khác"
        orderDetails.put("tel", userAddress.getUser().getPhone()); // Đặt số điện thoại người nhận

        // Calculate total weight (in grams)
        int totalWeightGrams = 0; // Khởi tạo tổng trọng lượng bằng 0
        for (OrderItem item : orderItems) { // Duyệt qua danh sách mặt hàng
            Integer productWeightGrams = item.getProduct().getWeight(); // Lấy trọng lượng sản phẩm
            if (productWeightGrams == null || productWeightGrams < 1) { // Kiểm tra nếu trọng lượng không hợp lệ
                productWeightGrams = 500; // Gán trọng lượng mặc định 500g
            }
            totalWeightGrams += productWeightGrams * item.getQuantity(); // Cộng trọng lượng của từng mặt hàng
        }

        // Convert to kg
        double totalWeightKg = totalWeightGrams / 1000.0; // Chuyển trọng lượng sang kg
        orderDetails.put("total_weight", totalWeightKg); // Đặt tổng trọng lượng vào chi tiết đơn hàng

        // Products
        List<Map<String, Object>> products = orderItems.stream().map(item -> { // Ánh xạ danh sách mặt hàng sang danh
                                                                               // sách sản phẩm GHTK
            Map<String, Object> product = new HashMap<>(); // Tạo đối tượng sản phẩm
            product.put("name", item.getProduct().getName()); // Đặt tên sản phẩm
            Integer productWeightGrams = item.getProduct().getWeight(); // Lấy trọng lượng sản phẩm
            if (productWeightGrams == null || productWeightGrams < 1) { // Kiểm tra nếu trọng lượng không hợp lệ
                productWeightGrams = 500; // Gán trọng lượng mặc định 500g
            }
            double productWeightKg = productWeightGrams / 1000.0; // Chuyển trọng lượng sang kg
            product.put("weight", productWeightKg); // Đặt trọng lượng sản phẩm
            product.put("quantity", item.getQuantity()); // Đặt số lượng sản phẩm
            return product; // Trả về đối tượng sản phẩm
        }).collect(Collectors.toList()); // Thu thập kết quả thành danh sách
        orderDetails.put("products", products); // Đặt danh sách sản phẩm vào chi tiết đơn hàng

        // Order value
        long orderValue = productTotal.longValue(); // Lấy giá trị đơn hàng từ productTotal
        if (orderValue < 1) { // Kiểm tra nếu giá trị nhỏ hơn 1
            orderValue = 1; // Đặt giá trị tối thiểu là 1
        } else if (orderValue > 20000000) { // Kiểm tra nếu giá trị vượt 20 triệu
            orderValue = 20000000; // Giới hạn giá trị ở 20 triệu
        }
        orderDetails.put("value", orderValue); // Đặt giá trị đơn hàng

        // COD amount (pick_money)
        BigDecimal totalPrice = order.getTotal_price(); // Lấy tổng giá từ Order
        long pickMoney = totalPrice.longValue(); // Chuyển tổng giá sang long
        if (pickMoney < 1) { // Kiểm tra nếu tổng giá nhỏ hơn 1
            pickMoney = 1; // Đặt tổng giá tối thiểu là 1
        } else if (pickMoney > 20000000) { // Kiểm tra nếu tổng giá vượt 20 triệu
            pickMoney = 20000000; // Giới hạn tổng giá ở 20 triệu
        }
        orderDetails.put("deliver_option", "cod"); // Đặt phương thức giao hàng là COD
        orderDetails.put("is_cod", true); // Đánh dấu đơn hàng sử dụng COD
        orderDetails.put("pick_money", pickMoney); // Đặt số tiền COD
        orderDetails.put("is_freeship", 1); // Đặt trạng thái miễn phí vận chuyển (1 = miễn phí)

        requestBody.put("order", orderDetails); // Đặt chi tiết đơn hàng vào body yêu cầu

        HttpHeaders headers = new HttpHeaders(); // Tạo đối tượng HttpHeaders
        headers.set("Token", ghtkToken); // Đặt token GHTK vào header
        headers.set("Content-Type", "application/json; charset=UTF-8"); // Đặt loại nội dung là JSON với mã hóa UTF-8
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers); // Tạo HttpEntity với body và
                                                                                         // header

        try {
            System.out.println("GHTK request payload: " + requestBody); // In payload để debug
            ResponseEntity<Map> response = restTemplate.postForEntity(ghtkUrl + "/services/shipment/order", entity,
                    Map.class); // Gửi yêu cầu POST để tạo đơn hàng
            Map<String, Object> responseBody = response.getBody(); // Lấy body từ phản hồi
            if (responseBody != null && responseBody.containsKey("success") && (boolean) responseBody.get("success")) { // Kiểm
                                                                                                                        // tra
                                                                                                                        // nếu
                                                                                                                        // yêu
                                                                                                                        // cầu
                                                                                                                        // thành
                                                                                                                        // công
                return (String) ((Map) responseBody.get("order")).get("label"); // Trả về mã đơn hàng GHTK (label)
            } else { // Nếu yêu cầu thất bại
                System.out.println("Không thể tạo đơn hàng GHTK: "
                        + (responseBody != null ? responseBody.toString() : "No response")); // In lỗi
                return null; // Trả về null nếu không tạo được đơn hàng
            }
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi khi gửi yêu cầu
            System.out.println("Lỗi khi tạo đơn hàng GHTK: " + e.getMessage()); // In lỗi
            return null; // Trả về null khi có lỗi
        }
    }
}