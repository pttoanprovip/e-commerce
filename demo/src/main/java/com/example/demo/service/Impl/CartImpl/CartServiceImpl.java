package com.example.demo.service.Impl.CartImpl;

import com.example.demo.dto.req.Cart.CartRequest;
import com.example.demo.dto.req.Cart.CartProductRequest;
import com.example.demo.dto.res.Cart.CartProductResponse;
import com.example.demo.dto.res.Cart.CartResponse;
import com.example.demo.entity.Cart.Cart;
import com.example.demo.entity.Cart.Cart_Product;
import com.example.demo.entity.Product.Product;
import com.example.demo.entity.User.User;
import com.example.demo.repository.Cart.CartProductRepository;
import com.example.demo.repository.Cart.CartRepository;
import com.example.demo.repository.Product.ProductRepository;
import com.example.demo.repository.User.UserRepository;
import com.example.demo.service.Cart.CartService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    // private final ModelMapper modelMapper;
    private final CartProductRepository cartProductRepository;

    // @Autowired
    public CartServiceImpl(CartRepository cartRepository,
            ProductRepository productRepository, UserRepository userRepository, // ModelMapper modelMapper,
            CartProductRepository cartProductRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartProductRepository = cartProductRepository;
        // this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    @PreAuthorize("T(String).valueOf(#cartRequest.userId) == authentication.principal.claims['sub']")
    public CartResponse addItem(CartRequest cartRequest) {
        try {
            // // Step 1: Get the cart based on userId or create a new one if it doesn't
            // exist
            // Cart cart = cartRepository.findById(cartRequest.getUserId())
            // .orElseGet(() -> {
            // User user = userRepository.findById(cartRequest.getUserId())
            // .orElseThrow(() -> new RuntimeException("User not found"));
            // Cart newCart = new Cart();
            // newCart.setUser(user);
            // newCart.setCart_products(new ArrayList<>()); // Initialize the cart_products
            // list
            // return newCart;
            // });

            // new

            // Bước 1: Lấy giỏ hàng dựa trên userId hoặc tạo mới nếu giỏ hàng chưa tồn tại
            Cart cart;
            Optional<Cart> existingCart = cartRepository.findByUserId(cartRequest.getUserId());

            if (existingCart.isPresent()) {
                cart = existingCart.get(); // Sử dụng giỏ hàng hiện tại
            } else {
                User user = userRepository.findById(cartRequest.getUserId())
                        .orElseThrow(() -> new RuntimeException("Not found user"));
                cart = new Cart();
                cart.setUser(user);
                cart.setCart_products(new ArrayList<>()); // Khởi tạo danh sách trống
            }

            // Bước 2: Xử lý danh sách sản phẩm trong yêu cầu
            List<Cart_Product> existingCartProducts = cart.getCart_products();
            for (CartProductRequest cartProductRequest : cartRequest.getCartProduct()) {
                Product product = productRepository.findById(cartProductRequest.getProductId())
                        .orElseThrow(() -> new RuntimeException("Not found product"));

                // Kiểm tra xem sản phẩm đã tồn tại trong giỏ hàng chưa
                Cart_Product existingCartProduct = existingCartProducts.stream()
                        .filter(cp -> cp.getProduct().equals(product))
                        .findFirst()
                        .orElse(null);

                if (existingCartProduct != null) {
                    // Cập nhật số lượng nếu sản phẩm đã tồn tại trong giỏ hàng
                    existingCartProduct
                            .setQuantity(existingCartProduct.getQuantity() + cartProductRequest.getQuantity());
                } else {
                    // Thêm sản phẩm mới vào giỏ hàng
                    Cart_Product newCartProduct = new Cart_Product();
                    newCartProduct.setCart(cart);
                    newCartProduct.setProduct(product);
                    newCartProduct.setQuantity(cartProductRequest.getQuantity());
                    existingCartProducts.add(newCartProduct);
                }
            }

            // Tính tổng giá và cập nhật lại
            double totalPrice = cart.getCart_products().stream()
                    .mapToDouble(cart_Product -> cart_Product.getProduct().getPrice() * cart_Product.getQuantity())
                    .sum();

            cart.setTotal_price(totalPrice);

            // Lưu giỏ hàng đã được cập nhật
            cart.setCart_products(existingCartProducts);
            Cart savedCart = cartRepository.save(cart);

            // Tạo phản hồi CartResponse thủ công
            CartResponse cartResponse = new CartResponse();
            cartResponse.setId(savedCart.getId());
            cartResponse.setCartProduct(savedCart.getCart_products().stream().map(cartProduct -> {
                CartProductResponse cartProductResponse = new CartProductResponse();
                cartProductResponse.setId(cartProduct.getProduct().getId());
                cartProductResponse.setProductName(cartProduct.getProduct().getName());
                cartProductResponse.setQuantity(cartProduct.getQuantity());
                cartProductResponse.setPrice(cartProduct.getProduct().getPrice());
                return cartProductResponse;
            }).collect(Collectors.toList()));

            cartResponse.setTotal_price(savedCart.getTotal_price());

            return cartResponse;

            // Chuyển đổi giỏ hàng đã lưu thành CartResponse và trả về
            // return modelMapper.map(savedCart, CartResponse.class);
        } catch (Exception e) {
            logger.error("Lỗi khi thêm sản phẩm vào giỏ hàng", e);
            throw e;
        }
    }

    @Override
    @Transactional
    @PreAuthorize("T(String).valueOf(#cartRequest.userId) == authentication.principal.claims['sub'] or hasRole('Admin')")
    public CartResponse removeItem(CartRequest cartRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        Jwt jwt = (Jwt) jwtAuth.getPrincipal();

        // Lấy userId từ JWT claims
        String userIdFromToken = jwt.getClaim("sub");

        System.out.println("User ID from Request: " + cartRequest.getUserId());
        System.out.println("User ID from Authentication: " + userIdFromToken);

        // Tìm giỏ hàng của người dùng
        Cart cart = cartRepository.findByUserId(cartRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // Danh sách sản phẩm cần xóa
        List<Cart_Product> productsToRemove = new ArrayList<>();
        double totalPrice = 0.0;

        // Duyệt qua từng sản phẩm trong giỏ hàng
        cart.getCart_products().forEach(cartProduct -> {
            cartRequest.getCartProduct().forEach(requestProduct -> {
                if (cartProduct.getProduct().getId() == requestProduct.getProductId()) {
                    if (requestProduct.getQuantity() == null || requestProduct.getQuantity() <= 0) {
                        // Nếu không có quantity trong request, thêm sản phẩm vào danh sách cần xóa
                        productsToRemove.add(cartProduct);
                    } else {
                        // Nếu có quantity, giảm số lượng sản phẩm
                        int newQuantity = cartProduct.getQuantity() - requestProduct.getQuantity();
                        if (newQuantity > 0) {
                            cartProduct.setQuantity(newQuantity);
                        } else {
                            productsToRemove.add(cartProduct);
                        }
                    }
                }
            });
        });

        // Xóa tất cả sản phẩm trong danh sách cần xóa
        cart.getCart_products().removeAll(productsToRemove);
        cartProductRepository.deleteAll(productsToRemove);

        // Tính lại total sau khi xóa sản phẩm khỏi giỏ hàng
        for (Cart_Product cartProduct : cart.getCart_products()) {
            totalPrice += cartProduct.getProduct().getPrice() * cartProduct.getQuantity();
        }

        cart.setTotal_price(totalPrice);

        // Lưu thay đổi giỏ hàng
        Cart updatedCart = cartRepository.save(cart);

        // Tạo response trả về
        CartResponse response = new CartResponse();
        response.setId(updatedCart.getId());

        List<CartProductResponse> cartProductResponses = updatedCart.getCart_products().stream()
                .map(cp -> {
                    CartProductResponse productResponse = new CartProductResponse();
                    productResponse.setId(cp.getProduct().getId()); // Đảm bảo rằng ID sản phẩm được đặt
                    productResponse.setProductName(cp.getProduct().getName());
                    productResponse.setQuantity(cp.getQuantity());
                    productResponse.setPrice(cp.getProduct().getPrice());
                    return productResponse;
                }).collect(Collectors.toList());

        response.setCartProduct(cartProductResponses);

        // Đặt giá trị total_price vào CartResponse
        response.setTotal_price(updatedCart.getTotal_price());

        return response;
    }

    @Override
    @PreAuthorize("T(String).valueOf(@cartRepository.findById(#id).orElseThrow().getUser().getId()) == authentication.principal.claims['sub'] or hasRole('Admin')")
    public CartResponse getCart(int id) {
        // Lấy giỏ hàng dựa trên id
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

        // Tạo đối tượng CartResponse
        CartResponse cartResponse = new CartResponse();
        cartResponse.setId(cart.getId());

        // Ánh xạ danh sách Cart_Product sang CartProductResponse
        List<CartProductResponse> cartProductResponses = cart.getCart_products().stream().map(cartProduct -> {
            CartProductResponse cartProductResponse = new CartProductResponse();
            cartProductResponse.setId(cartProduct.getProduct().getId()); // Đảm bảo rằng ID sản phẩm được đặt
            cartProductResponse.setProductName(cartProduct.getProduct().getName());
            cartProductResponse.setQuantity(cartProduct.getQuantity());
            cartProductResponse.setPrice(cartProduct.getProduct().getPrice());
            return cartProductResponse;
        }).collect(Collectors.toList());

        cartResponse.setCartProduct(cartProductResponses);

        // Đặt giá trị total_price vào CartResponse
        cartResponse.setTotal_price(cart.getTotal_price());

        return cartResponse;
    }

}