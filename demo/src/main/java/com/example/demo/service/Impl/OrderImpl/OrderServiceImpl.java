package com.example.demo.service.Impl.OrderImpl; // Định nghĩa package chứa class này

import java.math.BigDecimal; // Import BigDecimal để xử lý số tiền chính xác
import java.util.List; // Import lớp List để sử dụng danh sách
import java.util.stream.Collectors; // Import Collectors để thu thập kết quả từ stream

import org.modelmapper.ModelMapper; // Import ModelMapper để ánh xạ giữa DTO và Entity
import org.springframework.scheduling.annotation.Scheduled; // Import annotation Scheduled để lập lịch chạy phương thức
import org.springframework.security.access.prepost.PreAuthorize; // Import annotation PreAuthorize để kiểm tra quyền trước khi thực thi phương thức
import org.springframework.security.core.Authentication; // Import lớp Authentication để lấy thông tin xác thực
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder để truy cập ngữ cảnh bảo mật
import org.springframework.security.oauth2.jwt.Jwt; // Import lớp Jwt để xử lý token JWT
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken; // Import JwtAuthenticationToken để xác thực JWT
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch

import com.example.demo.dto.req.Discount.ApplyDiscountRequest; // Import DTO ApplyDiscountRequest để áp dụng mã giảm giá
import com.example.demo.dto.req.Order.OrderRequest; // Import DTO OrderRequest để nhận dữ liệu đầu vào
import com.example.demo.dto.res.Order.OrderItemResponse; // Import DTO OrderItemResponse để trả về chi tiết mặt hàng
import com.example.demo.dto.res.Order.OrderResponse; // Import DTO OrderResponse để trả về dữ liệu đầu ra
import com.example.demo.entity.Cart.Cart; // Import lớp Cart từ entity
import com.example.demo.entity.Cart.Cart_Product; // Import lớp Cart_Product từ entity
import com.example.demo.entity.Order.Order; // Import lớp Order từ entity
import com.example.demo.entity.Order.OrderItem; // Import lớp OrderItem từ entity
import com.example.demo.entity.Product.Product; // Import lớp Product từ entity
import com.example.demo.entity.User.User; // Import lớp User từ entity
import com.example.demo.entity.User.UserAddress; // Import lớp UserAddress từ entity
import com.example.demo.enums.OrderStatus; // Import enum OrderStatus để quản lý trạng thái đơn hàng
import com.example.demo.repository.Cart.CartRepository; // Import CartRepository để truy vấn Cart
import com.example.demo.repository.Order.OrderItemRepository; // Import OrderItemRepository để truy vấn OrderItem
import com.example.demo.repository.Order.OrderRepository; // Import OrderRepository để truy vấn Order
import com.example.demo.repository.Product.ProductRepository; // Import ProductRepository để truy vấn Product
import com.example.demo.repository.User.UserRepository; // Import UserRepository để truy vấn User
import com.example.demo.service.Dicount.DiscountService; // Import DiscountService để xử lý giảm giá
import com.example.demo.service.Order.GHTKService; // Import GHTKService để tích hợp với dịch vụ vận chuyển GHTK
import com.example.demo.service.Order.OrderService; // Import interface OrderService mà lớp này triển khai

import lombok.RequiredArgsConstructor; // Import annotation RequiredArgsConstructor để tự động tạo constructor với các field final

@Service // Đánh dấu lớp này là một Spring Service
@RequiredArgsConstructor // Tự động tạo constructor để tiêm các dependency final
public class OrderServiceImpl implements OrderService { // Lớp triển khai interface OrderService

    private final OrderRepository orderRepository; // Khai báo biến OrderRepository để tương tác với cơ sở dữ liệu Order
    private final OrderItemRepository orderItemRepository; // Khai báo biến OrderItemRepository để tương tác với cơ sở
                                                           // dữ liệu OrderItem
    private final ProductRepository productRepository; // Khai báo biến ProductRepository để tương tác với cơ sở dữ liệu
                                                       // Product
    private final UserRepository userRepository; // Khai báo biến UserRepository để tương tác với cơ sở dữ liệu User
    private final CartRepository cartRepository; // Khai báo biến CartRepository để tương tác với cơ sở dữ liệu Cart
    private final DiscountService discountService; // Khai báo biến DiscountService để xử lý mã giảm giá
    private final ModelMapper modelMapper; // Khai báo biến ModelMapper để ánh xạ đối tượng
    private final GHTKService ghtkService; // Khai báo biến GHTKService để tích hợp với dịch vụ vận chuyển GHTK

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("T(String).valueOf(#orderRequest.userId) == authentication.principal.claims['sub']") // Kiểm tra
                                                                                                       // quyền: chỉ
                                                                                                       // người dùng sở
                                                                                                       // hữu userId
                                                                                                       // được đặt hàng
    public OrderResponse placeOrder(OrderRequest orderRequest) { // Phương thức tạo đơn hàng mới
        User user = userRepository.findById(orderRequest.getUserId()) // Tìm User theo userId từ request
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng")); // Ném ngoại lệ nếu không tìm
                                                                                       // thấy user

        // Tạo đơn hàng mới
        Order order = new Order(); // Tạo đối tượng Order mới
        order.setUser(user); // Liên kết đơn hàng với User
        order.setOrderStatus(OrderStatus.Pending); // Gán trạng thái đơn hàng là PENDING
        Order savedOrder = orderRepository.save(order); // Lưu Order vào cơ sở dữ liệu

        // Lấy danh sách sản phẩm
        List<OrderItem> orderItems; // Khai báo biến để lưu danh sách OrderItem
        if (orderRequest.getOrder_items() != null && !orderRequest.getOrder_items().isEmpty()) { // Kiểm tra nếu request
                                                                                                 // có danh sách sản
                                                                                                 // phẩm
            orderItems = orderRequest.getOrder_items().stream().map(productRequest -> { // Ánh xạ danh sách sản phẩm từ
                                                                                        // request sang OrderItem
                Product product = productRepository.findById(productRequest.getProductId()) // Tìm Product theo
                                                                                            // productId
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm")); // Ném ngoại lệ nếu không
                                                                                             // tìm thấy sản phẩm
                OrderItem orderItem = new OrderItem(); // Tạo đối tượng OrderItem mới
                orderItem.setOrder(savedOrder); // Liên kết OrderItem với Order
                orderItem.setProduct(product); // Gán Product cho OrderItem
                orderItem.setQuantity(productRequest.getQuantity()); // Gán số lượng từ request
                return orderItem; // Trả về OrderItem
            }).collect(Collectors.toList()); // Thu thập kết quả thành danh sách
        } else { // Nếu request không có danh sách sản phẩm, lấy từ giỏ hàng
            Cart cart = cartRepository.findByUserId(orderRequest.getUserId()) // Tìm Cart theo userId
                    .orElseThrow(() -> new RuntimeException("Giỏ hàng trống")); // Ném ngoại lệ nếu không tìm thấy giỏ
                                                                                // hàng
            List<Cart_Product> cartProducts = cart.getCart_products(); // Lấy danh sách sản phẩm trong giỏ hàng
            if (cartProducts.isEmpty()) { // Kiểm tra nếu giỏ hàng rỗng
                throw new RuntimeException("Giỏ hàng trống"); // Ném ngoại lệ
            }
            orderItems = cartProducts.stream().map(cartProduct -> { // Ánh xạ danh sách sản phẩm trong giỏ hàng sang
                                                                    // OrderItem
                Product product = productRepository.findById(cartProduct.getProduct().getId()) // Tìm Product theo ID
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm")); // Ném ngoại lệ nếu không
                                                                                             // tìm thấy sản phẩm
                OrderItem orderItem = new OrderItem(); // Tạo đối tượng OrderItem mới
                orderItem.setOrder(savedOrder); // Liên kết OrderItem với Order
                orderItem.setProduct(product); // Gán Product cho OrderItem
                orderItem.setQuantity(cartProduct.getQuantity()); // Gán số lượng từ giỏ hàng
                return orderItem; // Trả về OrderItem
            }).collect(Collectors.toList()); // Thu thập kết quả thành danh sách
            cartRepository.deleteById(orderRequest.getUserId()); // Xóa giỏ hàng sau khi tạo đơn hàng
        }

        orderItemRepository.saveAll(orderItems); // Lưu tất cả OrderItem vào cơ sở dữ liệu

        // Tính tổng giá sản phẩm (giá đã là VND)
        BigDecimal productTotal = orderItems.stream() // Tính tổng giá trị sản phẩm
                .map(orderItem -> BigDecimal.valueOf(orderItem.getProduct().getPrice()) // Lấy giá sản phẩm
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity()))) // Nhân với số lượng
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Cộng tất cả giá trị lại

        // Áp dụng mã giảm giá nếu có
        if (orderRequest.getDiscountCode() != null) { // Kiểm tra nếu có mã giảm giá
            ApplyDiscountRequest applyDiscountRequest = new ApplyDiscountRequest(); // Tạo đối tượng
                                                                                    // ApplyDiscountRequest
            applyDiscountRequest.setCode(orderRequest.getDiscountCode()); // Gán mã giảm giá từ request
            double discountedPrice = discountService.applyDiscount(applyDiscountRequest, productTotal.doubleValue()); // Áp
                                                                                                                      // dụng
                                                                                                                      // giảm
                                                                                                                      // giá
            productTotal = BigDecimal.valueOf(discountedPrice); // Cập nhật tổng giá sau giảm giá
        }

        // Lấy địa chỉ mặc định
        UserAddress defaultAddress = user.getUserAddress().stream() // Lấy danh sách địa chỉ của user
                .filter(UserAddress::isDefaultAddress) // Lọc địa chỉ mặc định
                .findFirst() // Lấy địa chỉ đầu tiên
                .orElseThrow(() -> new RuntimeException("Người dùng không có địa chỉ mặc định")); // Ném ngoại lệ nếu
                                                                                                  // không có địa chỉ
                                                                                                  // mặc định

        // Tính phí vận chuyển
        BigDecimal shippingFee = BigDecimal.ZERO; // Khởi tạo phí vận chuyển bằng 0
        try {
            int totalWeight = orderItems.stream().mapToInt(item -> item.getQuantity() * 500).sum(); // Tính tổng trọng
                                                                                                    // lượng (giả định
                                                                                                    // 500g/sản phẩm)
            shippingFee = ghtkService.calculateShippingFee( // Gọi dịch vụ GHTK để tính phí vận chuyển
                    "Hồ Chí Minh", "Quận 7", // Địa chỉ kho mặc định
                    defaultAddress.getCity(), defaultAddress.getAddress(), // Địa chỉ giao hàng
                    totalWeight); // Tổng trọng lượng
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi khi tính phí
            System.err.println("Lỗi tính phí vận chuyển: " + e.getMessage()); // In lỗi ra console
            shippingFee = BigDecimal.valueOf(30000); // Gán phí vận chuyển mặc định là 30,000 VND
        }

        // Tính tổng giá bao gồm phí vận chuyển
        BigDecimal totalPrice = productTotal.add(shippingFee); // Cộng tổng giá sản phẩm với phí vận chuyển

        // Kiểm tra giới hạn GHTK
        if (totalPrice.compareTo(BigDecimal.valueOf(20_000_000)) > 0) { // Kiểm tra nếu tổng giá vượt 20 triệu VND
            System.err.println("Cảnh báo: Tổng giá " + totalPrice + " VND vượt giới hạn GHTK 20.000.000 VND"); // In
                                                                                                               // cảnh
                                                                                                               // báo
            totalPrice = BigDecimal.valueOf(20_000_000); // Giới hạn tổng giá ở 20 triệu VND
        }

        savedOrder.setTotal_price(totalPrice); // Gán tổng giá cho Order
        orderRepository.save(savedOrder); // Lưu Order với tổng giá

        // Tạo đơn hàng GHTK
        try {
            String ghtkOrderCode = ghtkService.createOrderWithGHTK(savedOrder, orderItems, defaultAddress,
                    productTotal); // Tạo đơn hàng với GHTK
            if (ghtkOrderCode != null) { // Kiểm tra nếu tạo mã đơn hàng GHTK thành công
                savedOrder.setGhtkOrderCode(ghtkOrderCode); // Gán mã đơn hàng GHTK
                savedOrder.setOrderStatus(OrderStatus.Processing); // Cập nhật trạng thái đơn hàng là PROCESSING
            } else { // Nếu không tạo được mã GHTK
                System.err.println("Không tạo được mã đơn hàng GHTK cho đơn hàng " + savedOrder.getId()); // In lỗi
                savedOrder.setOrderStatus(OrderStatus.Pending); // Giữ trạng thái PENDING
            }
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi khi tạo đơn hàng GHTK
            System.err.println("Không thể tạo đơn hàng GHTK: " + e.getMessage()); // In lỗi
            savedOrder.setOrderStatus(OrderStatus.Pending); // Giữ trạng thái PENDING
        }
        orderRepository.save(savedOrder); // Lưu Order với trạng thái và mã GHTK (nếu có)

        // Chuyển sang response
        OrderResponse orderResponse = modelMapper.map(savedOrder, OrderResponse.class); // Ánh xạ Order sang
                                                                                        // OrderResponse
        List<OrderItemResponse> orderItemResponses = orderItems.stream() // Ánh xạ danh sách OrderItem sang
                                                                         // OrderItemResponse
                .map(orderItem -> {
                    OrderItemResponse orderItemResponse = modelMapper.map(orderItem, OrderItemResponse.class); // Ánh xạ
                                                                                                               // OrderItem
                    orderItemResponse.setPrice(orderItem.getProduct().getPrice()); // Gán giá sản phẩm
                    return orderItemResponse; // Trả về OrderItemResponse
                })
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách
        orderResponse.setOrderItem(orderItemResponses); // Gán danh sách OrderItemResponse vào OrderResponse
        orderResponse.setUserFullName(savedOrder.getUser().getName()); // Gán tên người dùng vào OrderResponse

        return orderResponse; // Trả về OrderResponse
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(@orderRepository.findById(#id).orElseThrow().getUser().getId()) == authentication.principal.claims['sub']") // Kiểm
                                                                                                                                                                     // tra
                                                                                                                                                                     // quyền:
                                                                                                                                                                     // chỉ
                                                                                                                                                                     // Admin
                                                                                                                                                                     // hoặc
                                                                                                                                                                     // người
                                                                                                                                                                     // dùng
                                                                                                                                                                     // sở
                                                                                                                                                                     // hữu
                                                                                                                                                                     // đơn
                                                                                                                                                                     // hàng
                                                                                                                                                                     // được
                                                                                                                                                                     // truy
                                                                                                                                                                     // cập
    public OrderResponse getOrderById(int id) { // Phương thức lấy đơn hàng theo ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Lấy đối tượng
                                                                                                // Authentication từ ngữ
                                                                                                // cảnh bảo mật
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication; // Ép kiểu sang JwtAuthenticationToken
        Jwt jwt = (Jwt) jwtAuth.getPrincipal(); // Lấy đối tượng JWT từ authentication

        String userIdFromToken = jwt.getClaim("sub"); // Lấy userId từ token JWT
        System.out.println("User ID from token: " + userIdFromToken); // In userId để debug
        System.out.println("Checking order ID: " + id); // In ID đơn hàng để debug

        // Lấy Order theo Id
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found")); // Tìm
                                                                                                               // Order
                                                                                                               // theo
                                                                                                               // ID,
                                                                                                               // ném
                                                                                                               // ngoại
                                                                                                               // lệ nếu
                                                                                                               // không
                                                                                                               // tìm
                                                                                                               // thấy

        // Chuyển đổi thông tin từ Order sang OrderResponse
        OrderResponse orderResponses = modelMapper.map(order, OrderResponse.class); // Ánh xạ Order sang OrderResponse

        orderResponses.setUserFullName(order.getUser().getName()); // Gán tên người dùng vào OrderResponse

        // Lấy danh sách OrderItem từ order
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream() // Ánh xạ danh sách OrderItem sang
                                                                                    // OrderItemResponse
                .map(orderItem -> {
                    OrderItemResponse orderItemResponse = modelMapper.map(orderItem, OrderItemResponse.class); // Ánh xạ
                                                                                                               // OrderItem
                    orderItemResponse.setProductName(orderItem.getProduct().getName()); // Gán tên sản phẩm
                    orderItemResponse.setPrice(orderItem.getProduct().getPrice()); // Gán giá sản phẩm
                    return orderItemResponse; // Trả về OrderItemResponse
                }).collect(Collectors.toList()); // Thu thập kết quả thành danh sách

        // Đặt orderItem vào Response
        orderResponses.setOrderItem(orderItemResponses); // Gán danh sách OrderItemResponse vào OrderResponse

        return orderResponses; // Trả về OrderResponse
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(#userId) == authentication.principal.claims['sub']") // Kiểm
                                                                                                              // tra
                                                                                                              // quyền:
                                                                                                              // chỉ
                                                                                                              // Admin
                                                                                                              // hoặc
                                                                                                              // người
                                                                                                              // dùng sở
                                                                                                              // hữu
                                                                                                              // userId
                                                                                                              // được
                                                                                                              // truy
                                                                                                              // cập
    public List<OrderResponse> getUserById(int userId) { // Phương thức lấy danh sách đơn hàng của người dùng theo
                                                         // userId
        // Lấy tất cả order của user theo userId
        List<Order> orders = orderRepository.findByUserId(userId) // Tìm danh sách Order theo userId
                .orElseThrow(() -> new RuntimeException("Orders not found")); // Ném ngoại lệ nếu không tìm thấy đơn
                                                                              // hàng

        // Ánh xạ sang Response
        return orders.stream().map(order -> { // Ánh xạ từng Order sang OrderResponse
            // Chuyển đổi order sang orderResponse
            OrderResponse orderResponse = modelMapper.map(order, OrderResponse.class); // Ánh xạ Order

            // Lấy danh sách OrderItem từ order và ánh xạ sang OrderItemResponse
            List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream() // Ánh xạ danh sách OrderItem
                    .map(orderItem -> {
                        OrderItemResponse orderItemResponse = modelMapper.map(orderItem, OrderItemResponse.class); // Ánh
                                                                                                                   // xạ
                                                                                                                   // OrderItem
                        orderItemResponse.setProductName(orderItem.getProduct().getName()); // Gán tên sản phẩm
                        orderItemResponse.setPrice(orderItem.getProduct().getPrice()); // Gán giá sản phẩm
                        return orderItemResponse; // Trả về OrderItemResponse
                    }).collect(Collectors.toList()); // Thu thập kết quả thành danh sách

            orderResponse.setOrderItem(orderItemResponses); // Gán danh sách OrderItemResponse vào OrderResponse

            orderResponse.setUserFullName(order.getUser().getName()); // Gán tên người dùng vào OrderResponse

            return orderResponse; // Trả về OrderResponse
        }).collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @Scheduled(fixedRate = 60000) // Lập lịch chạy phương thức này mỗi 60 giây
    public void updateOrderStatus() { // Phương thức cập nhật trạng thái đơn hàng
        List<Order> pendingOrders = orderRepository.findByOrderStatus(OrderStatus.Pending); // Tìm tất cả đơn hàng có
                                                                                            // trạng thái PENDING
        for (Order order : pendingOrders) { // Duyệt qua từng đơn hàng
            String ghtkOrderCode = order.getGhtkOrderCode(); // Lấy mã đơn hàng GHTK
            if (ghtkOrderCode == null || ghtkOrderCode.isEmpty()) { // Kiểm tra nếu không có mã GHTK
                System.out.println("Order " + order.getId() + " does not have GHTK order code."); // In thông báo
                continue; // Bỏ qua đơn hàng này
            }

            try {
                OrderStatus newStatus = ghtkService.getOrderStatusFromGHTK(ghtkOrderCode); // Lấy trạng thái mới từ GHTK
                if (!newStatus.equals(order.getOrderStatus())) { // Kiểm tra nếu trạng thái mới khác trạng thái hiện tại
                    order.setOrderStatus(newStatus); // Cập nhật trạng thái mới
                    orderRepository.save(order); // Lưu Order với trạng thái mới
                    System.out.println("Updated order " + order.getId() + " status to " + newStatus); // In thông báo
                                                                                                      // cập nhật
                }
            } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi khi lấy trạng thái
                System.err.println("Error updating order " + order.getId() + ": " + e.getMessage()); // In lỗi
            }
        }
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được truy cập
    public List<OrderResponse> getAllOrders() { // Phương thức lấy danh sách tất cả đơn hàng
        List<Order> orders = orderRepository.findAll(); // Lấy tất cả Order từ cơ sở dữ liệu
        return orders.stream().map(order -> modelMapper.map(order, OrderResponse.class)) // Ánh xạ từng Order sang
                                                                                         // OrderResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }
}