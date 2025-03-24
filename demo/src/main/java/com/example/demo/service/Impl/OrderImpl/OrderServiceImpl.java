package com.example.demo.service.Impl.OrderImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.req.Discount.ApplyDiscountRequest;
import com.example.demo.dto.req.Order.OrderRequest;
import com.example.demo.dto.res.Order.OrderItemResponse;
import com.example.demo.dto.res.Order.OrderResponse;
import com.example.demo.entity.Cart.Cart;
import com.example.demo.entity.Cart.Cart_Product;
import com.example.demo.entity.Order.Order;
import com.example.demo.entity.Order.OrderItem;
import com.example.demo.entity.Product.Product;
import com.example.demo.entity.User.User;
import com.example.demo.enums.OrderStatus;
import com.example.demo.repository.Cart.CartRepository;
import com.example.demo.repository.Order.OrderItemRepository;
import com.example.demo.repository.Order.OrderRepository;
import com.example.demo.repository.Product.ProductRepository;
import com.example.demo.repository.User.UserRepository;
import com.example.demo.service.Dicount.DiscountService;
import com.example.demo.service.Order.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final DiscountService discountService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    @PreAuthorize("T(String).valueOf(#orderRequest.userId) == authentication.principal.claims['sub']")
    public OrderResponse placeOrder(OrderRequest orderRequest) {

        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tạo order mới
        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.Pending);

        // Lưu đơn hàng
        Order savedOrder = orderRepository.save(order);

        // Lấy danh sách sản phẩm
        List<OrderItem> orderItems;
        if (orderRequest.getOrder_items() != null && !orderRequest.getOrder_items().isEmpty()) {
            // Trường hợp sản phẩm được chọn bên ngoài
            orderItems = orderRequest.getOrder_items().stream().map(productRequest -> {
                Product product = productRepository.findById(productRequest.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setProduct(product);
                orderItem.setQuantity(productRequest.getQuantity());

                return orderItem;
            }).collect(Collectors.toList());
        } else {
            // Trường hợp sản phẩm lấy từ giỏ hàng
            Cart cart = cartRepository.findByUserId(orderRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("Cart is Empty"));

            List<Cart_Product> cartProducts = cart.getCart_products();
            if (cartProducts.isEmpty()) {
                throw new RuntimeException("Cart is empty");
            }

            // Chuyển đổi CartProductRequest thành OrderItem
            orderItems = cartProducts.stream().map(CartProduct -> {
                Product product = productRepository.findById(CartProduct.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setProduct(product);
                orderItem.setQuantity(CartProduct.getQuantity());
                return orderItem;
            }).collect(Collectors.toList());

            cartRepository.deleteById(orderRequest.getUserId());
        }

        // Lưu các mục sản phẩm vào db
        orderItemRepository.saveAll(orderItems);

        // Tính tổng giá và cập nhật lại
        double totalPrice = orderItems.stream()
                .mapToDouble(orderItem -> orderItem.getProduct().getPrice() * orderItem.getQuantity()).sum();

        // Áp dụng mã giảm giá nếu có
        if (orderRequest.getDiscountCode() != null) {
            ApplyDiscountRequest applyDiscountRequest = new ApplyDiscountRequest();
            applyDiscountRequest.setCode(orderRequest.getDiscountCode());
            totalPrice = discountService.applyDiscount(applyDiscountRequest, totalPrice);
        }

        savedOrder.setTotal_price(totalPrice);
        orderRepository.save(savedOrder);

        // Chuyển đổi sang response
        OrderResponse orderResponse = modelMapper.map(savedOrder, OrderResponse.class);
        // List<OrderItemResponse> orderItemResponses = orderItems.stream()
        // .map(orderItem -> modelMapper.map(orderItem, OrderItemResponse.class))
        // .collect(Collectors.toList());
        List<OrderItemResponse> orderItemResponses = orderItems.stream()
                .map(orderItem -> {
                    // Ánh xạ và gắn thêm giá sản phẩm
                    OrderItemResponse orderItemResponse = modelMapper.map(orderItem, OrderItemResponse.class);
                    orderItemResponse.setPrice(orderItem.getProduct().getPrice());
                    return orderItemResponse;
                })
                .collect(Collectors.toList());

        orderResponse.setOrderItem(orderItemResponses);

        orderResponse.setUserFullName(savedOrder.getUser().getName());

        return orderResponse;
    }

    @Override
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(@orderRepository.findById(#id).orElseThrow().getUser().getId()) == authentication.principal.claims['sub']")
    public OrderResponse getOrderById(int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        Jwt jwt = (Jwt) jwtAuth.getPrincipal();

        String userIdFromToken = jwt.getClaim("sub");
        System.out.println("User ID from token: " + userIdFromToken);
        System.out.println("Checking order ID: " + id);
        // Lấy Order theo Id
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

        // Chuyển đổi thông tin từ Order sang OrderResponse
        OrderResponse orderResponses = modelMapper.map(order, OrderResponse.class);

        orderResponses.setUserFullName(order.getUser().getName());

        // Lấy danh sách OrderItem từ order
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(orderItem -> {
                    OrderItemResponse orderItemResponse = modelMapper.map(orderItem, OrderItemResponse.class);
                    orderItemResponse.setProductName(orderItem.getProduct().getName());
                    orderItemResponse.setPrice(orderItem.getProduct().getPrice());
                    return orderItemResponse;
                }).collect(Collectors.toList());

        // Đặt orderItem vào Response
        orderResponses.setOrderItem(orderItemResponses);

        return orderResponses;
    }

    @Override
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(#userId) == authentication.principal.claims['sub']")
    public List<OrderResponse> getUserById(int userId) {
        // Lấy tất cả order của user theo userId
        List<Order> orders = orderRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Orders not found"));

        // Ánh xạ sang Response
        return orders.stream().map(order -> {
            // Chuyển đổi order sang orderResponse
            OrderResponse orderResponse = modelMapper.map(order, OrderResponse.class);

            // Lấy danh sách OrderItem từ order và ánh xạ sang OrderItemResponse
            List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                    .map(orderItem -> {
                        OrderItemResponse orderItemResponse = modelMapper.map(orderItem, OrderItemResponse.class);
                        orderItemResponse.setProductName(orderItem.getProduct().getName());
                        orderItemResponse.setPrice(orderItem.getProduct().getPrice());
                        return orderItemResponse;
                    }).collect(Collectors.toList());

            orderResponse.setOrderItem(orderItemResponses);

            orderResponse.setUserFullName(order.getUser().getName());

            return orderResponse;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateOrderStatus(int id, OrderStatus orderStatus) {
        Order updateOrder = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

        updateOrder.setOrderStatus(orderStatus);

        orderRepository.save(updateOrder);
    }

    @Override
    @PreAuthorize("hasRole('Admin')")
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(order -> modelMapper.map(order, OrderResponse.class))
                .collect(Collectors.toList());
    }

}
