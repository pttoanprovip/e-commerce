package com.example.demo.service.Impl.OrderImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
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

@Service
public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;
    private CartRepository cartRepository;
    private DiscountService discountService;
    private final ModelMapper modelMapper;

    //@Autowired
    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
            ProductRepository productRepository, UserRepository userRepository, CartRepository cartRepository,
            ModelMapper modelMapper, DiscountService discountService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.modelMapper = modelMapper;
        this.discountService = discountService;
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest) {

        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tạo order mới
        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.Pending);

        // Lưu đơn hàng
        Order savaOrder = orderRepository.save(order);

        // Lấy danh sách sản phẩm
        List<OrderItem> orderItems;
        if (orderRequest.getOrder_items() != null && !orderRequest.getOrder_items().isEmpty()) {
            // Trường hợp sản phẩm được chọn bên ngoài
            orderItems = orderRequest.getOrder_items().stream().map(productRequest -> {
                Product product = productRepository.findById(productRequest.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savaOrder);
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
                orderItem.setOrder(savaOrder);
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

        savaOrder.setTotal_price(totalPrice);
        orderRepository.save(savaOrder);

        // Chuyển đổi sang response
        OrderResponse orderResponse = modelMapper.map(savaOrder, OrderResponse.class);
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

        orderResponse.setUserFullName(savaOrder.getUser().getName());

        return orderResponse;
    }

    @Override
    public OrderResponse getOrderById(int id) {
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

}
