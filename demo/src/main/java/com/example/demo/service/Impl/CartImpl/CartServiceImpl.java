package com.example.demo.service.Impl.CartImpl; // Định nghĩa package chứa class này

import com.example.demo.dto.req.Cart.CartRequest; // Import DTO CartRequest để nhận dữ liệu đầu vào
import com.example.demo.dto.req.Cart.CartProductRequest; // Import DTO CartProductRequest để nhận thông tin sản phẩm trong giỏ hàng
import com.example.demo.dto.res.Cart.CartProductResponse; // Import DTO CartProductResponse để trả về thông tin sản phẩm trong giỏ hàng
import com.example.demo.dto.res.Cart.CartResponse; // Import DTO CartResponse để trả về dữ liệu đầu ra
import com.example.demo.entity.Cart.Cart; // Import lớp Cart từ entity
import com.example.demo.entity.Cart.Cart_Product; // Import lớp Cart_Product từ entity
import com.example.demo.entity.Product.Product; // Import lớp Product từ entity
import com.example.demo.entity.User.User; // Import lớp User từ entity
import com.example.demo.repository.Cart.CartProductRepository; // Import CartProductRepository để truy vấn Cart_Product
import com.example.demo.repository.Cart.CartRepository; // Import CartRepository để truy vấn Cart
import com.example.demo.repository.Product.ProductRepository; // Import ProductRepository để truy vấn Product
import com.example.demo.repository.User.UserRepository; // Import UserRepository để truy vấn User
import com.example.demo.service.Cart.CartService; // Import interface CartService mà lớp này triển khai

import org.springframework.security.access.prepost.PreAuthorize; // Import annotation PreAuthorize để kiểm tra quyền trước khi thực thi phương thức
import org.springframework.security.core.Authentication; // Import lớp Authentication để lấy thông tin xác thực
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder để truy cập ngữ cảnh bảo mật
import org.springframework.security.oauth2.jwt.Jwt; // Import lớp Jwt để xử lý token JWT
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken; // Import JwtAuthenticationToken để xác thực JWT
//import org.springframework.beans.factory.annotation.Autowired; // Dòng này bị comment, không sử dụng Autowired
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch
import org.slf4j.Logger; // Import Logger để ghi log
import org.slf4j.LoggerFactory; // Import LoggerFactory để tạo logger

import java.util.ArrayList; // Import ArrayList để sử dụng danh sách động
import java.util.List; // Import lớp List để sử dụng danh sách
import java.util.Optional; // Import Optional để xử lý kết quả truy vấn có thể null
import java.util.stream.Collectors; // Import Collectors để thu thập kết quả từ stream

@Service // Đánh dấu lớp này là một Spring Service
public class CartServiceImpl implements CartService { // Lớp triển khai interface CartService

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class); // Khởi tạo logger cho lớp

    private final CartRepository cartRepository; // Khai báo biến CartRepository để tương tác với cơ sở dữ liệu Cart
    private final ProductRepository productRepository; // Khai báo biến ProductRepository để tương tác với cơ sở dữ liệu
                                                       // Product
    private final UserRepository userRepository; // Khai báo biến UserRepository để tương tác với cơ sở dữ liệu User
    // private final ModelMapper modelMapper; // Dòng này bị comment, không sử dụng
    // ModelMapper
    private final CartProductRepository cartProductRepository; // Khai báo biến CartProductRepository để tương tác với
                                                               // cơ sở dữ liệu Cart_Product

    // Constructor để tiêm các dependency
    public CartServiceImpl(CartRepository cartRepository,
            ProductRepository productRepository, UserRepository userRepository, // ModelMapper modelMapper,
            CartProductRepository cartProductRepository) {
        this.cartRepository = cartRepository; // Gán CartRepository được tiêm vào biến instance
        this.productRepository = productRepository; // Gán ProductRepository được tiêm vào biến instance
        this.userRepository = userRepository; // Gán UserRepository được tiêm vào biến instance
        this.cartProductRepository = cartProductRepository; // Gán CartProductRepository được tiêm vào biến instance
        // this.modelMapper = modelMapper; // Dòng này bị comment, không sử dụng
        // ModelMapper
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("T(String).valueOf(#cartRequest.userId) == authentication.principal.claims['sub']") // Kiểm tra quyền:
                                                                                                      // chỉ người dùng
                                                                                                      // sở hữu userId
                                                                                                      // được thêm sản
                                                                                                      // phẩm
    public CartResponse addItem(CartRequest cartRequest) { // Phương thức thêm sản phẩm vào giỏ hàng
        try {
            // Lấy hoặc tạo giỏ hàng
            Cart cart; // Khai báo biến Cart để lưu giỏ hàng
            Optional<Cart> existingCart = cartRepository.findByUserId(cartRequest.getUserId()); // Tìm Cart theo userId
            if (existingCart.isPresent()) { // Kiểm tra nếu giỏ hàng đã tồn tại
                cart = existingCart.get(); // Sử dụng giỏ hàng hiện tại
            } else { // Nếu giỏ hàng chưa tồn tại
                User user = userRepository.findById(cartRequest.getUserId()) // Tìm User theo userId
                        .orElseThrow(() -> new RuntimeException("Not found user")); // Ném ngoại lệ nếu không tìm thấy
                                                                                    // user
                cart = new Cart(); // Tạo giỏ hàng mới
                cart.setUser(user); // Liên kết giỏ hàng với User
                cart.setCart_products(new ArrayList<>()); // Khởi tạo danh sách sản phẩm trống
            }

            List<Cart_Product> existingCartProducts = cart.getCart_products(); // Lấy danh sách sản phẩm hiện tại trong
                                                                               // giỏ hàng
            for (CartProductRequest cartProductRequest : cartRequest.getCartProduct()) { // Duyệt qua từng sản phẩm
                                                                                         // trong request
                Product product = productRepository.findById(cartProductRequest.getProductId()) // Tìm Product theo
                                                                                                // productId
                        .orElseThrow(() -> new RuntimeException("Not found product")); // Ném ngoại lệ nếu không tìm
                                                                                       // thấy sản phẩm

                Cart_Product existingCartProduct = existingCartProducts.stream() // Tìm sản phẩm trong giỏ hàng
                        .filter(cp -> cp.getProduct().equals(product)) // Lọc sản phẩm khớp với productId
                        .findFirst() // Lấy sản phẩm đầu tiên
                        .orElse(null); // Trả về null nếu không tìm thấy

                if (existingCartProduct != null) { // Kiểm tra nếu sản phẩm đã tồn tại trong giỏ hàng
                    // Thiết lập số lượng mới thay vì tăng
                    existingCartProduct.setQuantity(cartProductRequest.getQuantity()); // Cập nhật số lượng từ request
                } else { // Nếu sản phẩm chưa tồn tại
                    // Thêm sản phẩm mới
                    Cart_Product newCartProduct = new Cart_Product(); // Tạo đối tượng Cart_Product mới
                    newCartProduct.setCart(cart); // Liên kết Cart_Product với Cart
                    newCartProduct.setProduct(product); // Gán Product cho Cart_Product
                    newCartProduct.setQuantity(cartProductRequest.getQuantity()); // Gán số lượng từ request
                    existingCartProducts.add(newCartProduct); // Thêm Cart_Product vào danh sách
                }
            }

            // Tính tổng giá
            double totalPrice = cart.getCart_products().stream() // Tính tổng giá trị giỏ hàng
                    .mapToDouble(cartProduct -> cartProduct.getProduct().getPrice() * cartProduct.getQuantity()) // Nhân
                                                                                                                 // giá
                                                                                                                 // với
                                                                                                                 // số
                                                                                                                 // lượng
                    .sum(); // Cộng tất cả giá trị
            cart.setTotal_price(totalPrice); // Gán tổng giá cho Cart

            // Lưu giỏ hàng
            Cart savedCart = cartRepository.save(cart); // Lưu Cart vào cơ sở dữ liệu

            // Tạo phản hồi
            CartResponse cartResponse = new CartResponse(); // Tạo đối tượng CartResponse
            cartResponse.setId(savedCart.getId()); // Gán ID giỏ hàng
            cartResponse.setCartProduct(savedCart.getCart_products().stream().map(cartProduct -> { // Ánh xạ danh sách
                                                                                                   // Cart_Product sang
                                                                                                   // CartProductResponse
                CartProductResponse cartProductResponse = new CartProductResponse(); // Tạo đối tượng
                                                                                     // CartProductResponse
                cartProductResponse.setId(cartProduct.getProduct().getId()); // Gán ID sản phẩm
                cartProductResponse.setProductName(cartProduct.getProduct().getName()); // Gán tên sản phẩm
                cartProductResponse.setQuantity(cartProduct.getQuantity()); // Gán số lượng
                cartProductResponse.setPrice(cartProduct.getProduct().getPrice()); // Gán giá sản phẩm
                return cartProductResponse; // Trả về CartProductResponse
            }).collect(Collectors.toList())); // Thu thập kết quả thành danh sách
            cartResponse.setTotal_price(savedCart.getTotal_price()); // Gán tổng giá vào CartResponse

            return cartResponse; // Trả về CartResponse
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi
            logger.error("Lỗi khi thêm sản phẩm vào giỏ hàng", e); // Ghi log lỗi
            throw e; // Ném lại ngoại lệ để xử lý ở tầng trên
        }
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("T(String).valueOf(#cartRequest.userId) == authentication.principal.claims['sub'] or hasRole('Admin')") // Kiểm
                                                                                                                          // tra
                                                                                                                          // quyền:
                                                                                                                          // chỉ
                                                                                                                          // người
                                                                                                                          // dùng
                                                                                                                          // sở
                                                                                                                          // hữu
                                                                                                                          // userId
                                                                                                                          // hoặc
                                                                                                                          // Admin
                                                                                                                          // được
                                                                                                                          // xóa
                                                                                                                          // sản
                                                                                                                          // phẩm
    public CartResponse removeItem(CartRequest cartRequest) { // Phương thức xóa sản phẩm khỏi giỏ hàng
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Lấy đối tượng
                                                                                                // Authentication từ ngữ
                                                                                                // cảnh bảo mật
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication; // Ép kiểu sang JwtAuthenticationToken
        Jwt jwt = (Jwt) jwtAuth.getPrincipal(); // Lấy đối tượng JWT từ authentication

        // Lấy userId từ JWT claims
        String userIdFromToken = jwt.getClaim("sub"); // Lấy userId từ token JWT

        System.out.println("User ID from Request: " + cartRequest.getUserId()); // In userId từ request để debug
        System.out.println("User ID from Authentication: " + userIdFromToken); // In userId từ token để debug

        // Tìm giỏ hàng của người dùng
        Cart cart = cartRepository.findByUserId(cartRequest.getUserId()) // Tìm Cart theo userId
                .orElseThrow(() -> new RuntimeException("Cart not found")); // Ném ngoại lệ nếu không tìm thấy giỏ hàng

        // Danh sách sản phẩm cần xóa
        List<Cart_Product> productsToRemove = new ArrayList<>(); // Khai báo danh sách sản phẩm cần xóa
        double totalPrice = 0.0; // Khởi tạo tổng giá bằng 0

        // Duyệt qua từng sản phẩm trong giỏ hàng
        cart.getCart_products().forEach(cartProduct -> { // Duyệt qua danh sách sản phẩm trong giỏ hàng
            cartRequest.getCartProduct().forEach(requestProduct -> { // Duyệt qua danh sách sản phẩm từ request
                if (cartProduct.getProduct().getId() == requestProduct.getProductId()) { // Kiểm tra nếu sản phẩm khớp
                                                                                         // với productId từ request
                    if (requestProduct.getQuantity() == null || requestProduct.getQuantity() <= 0) { // Kiểm tra nếu
                                                                                                     // không có số
                                                                                                     // lượng hoặc số
                                                                                                     // lượng không hợp
                                                                                                     // lệ
                        // Nếu không có quantity trong request, thêm sản phẩm vào danh sách cần xóa
                        productsToRemove.add(cartProduct); // Thêm Cart_Product vào danh sách xóa
                    } else { // Nếu có số lượng
                        // Nếu có quantity, giảm số lượng sản phẩm
                        int newQuantity = cartProduct.getQuantity() - requestProduct.getQuantity(); // Tính số lượng mới
                        if (newQuantity > 0) { // Kiểm tra nếu số lượng mới vẫn lớn hơn 0
                            cartProduct.setQuantity(newQuantity); // Cập nhật số lượng
                        } else { // Nếu số lượng mới nhỏ hơn hoặc bằng 0
                            productsToRemove.add(cartProduct); // Thêm Cart_Product vào danh sách xóa
                        }
                    }
                }
            });
        });

        // Xóa tất cả sản phẩm trong danh sách cần xóa
        cart.getCart_products().removeAll(productsToRemove); // Xóa các sản phẩm khỏi danh sách Cart
        cartProductRepository.deleteAll(productsToRemove); // Xóa các Cart_Product khỏi cơ sở dữ liệu

        // Tính lại total sau khi xóa sản phẩm khỏi giỏ hàng
        for (Cart_Product cartProduct : cart.getCart_products()) { // Duyệt qua danh sách sản phẩm còn lại
            totalPrice += cartProduct.getProduct().getPrice() * cartProduct.getQuantity(); // Cộng giá trị từng sản phẩm
        }

        cart.setTotal_price(totalPrice); // Gán tổng giá mới cho Cart

        // Lưu thay đổi giỏ hàng
        Cart updatedCart = cartRepository.save(cart); // Lưu Cart đã cập nhật vào cơ sở dữ liệu

        // Tạo response trả về
        CartResponse response = new CartResponse(); // Tạo đối tượng CartResponse
        response.setId(updatedCart.getId()); // Gán ID giỏ hàng

        List<CartProductResponse> cartProductResponses = updatedCart.getCart_products().stream() // Ánh xạ danh sách
                                                                                                 // Cart_Product sang
                                                                                                 // CartProductResponse
                .map(cp -> {
                    CartProductResponse productResponse = new CartProductResponse(); // Tạo đối tượng
                                                                                     // CartProductResponse
                    productResponse.setId(cp.getProduct().getId()); // Gán ID sản phẩm
                    productResponse.setProductName(cp.getProduct().getName()); // Gán tên sản phẩm
                    productResponse.setQuantity(cp.getQuantity()); // Gán số lượng
                    productResponse.setPrice(cp.getProduct().getPrice()); // Gán giá sản phẩm
                    return productResponse; // Trả về CartProductResponse
                }).collect(Collectors.toList()); // Thu thập kết quả thành danh sách

        response.setCartProduct(cartProductResponses); // Gán danh sách CartProductResponse vào CartResponse

        // Đặt giá trị total_price vào CartResponse
        response.setTotal_price(updatedCart.getTotal_price()); // Gán tổng giá vào CartResponse

        return response; // Trả về CartResponse
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("T(String).valueOf(@cartRepository.findById(#id).orElseThrow().getUser().getId()) == authentication.principal.claims['sub'] or hasRole('Admin')") // Kiểm
                                                                                                                                                                    // tra
                                                                                                                                                                    // quyền:
                                                                                                                                                                    // chỉ
                                                                                                                                                                    // người
                                                                                                                                                                    // dùng
                                                                                                                                                                    // sở
                                                                                                                                                                    // hữu
                                                                                                                                                                    // giỏ
                                                                                                                                                                    // hàng
                                                                                                                                                                    // hoặc
                                                                                                                                                                    // Admin
                                                                                                                                                                    // được
                                                                                                                                                                    // truy
                                                                                                                                                                    // cập
    public CartResponse getCart(int id) { // Phương thức lấy giỏ hàng theo ID
        // Lấy giỏ hàng dựa trên id
        Cart cart = cartRepository.findById(id) // Tìm Cart theo ID
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng")); // Ném ngoại lệ nếu không tìm thấy
                                                                                     // giỏ hàng

        // Tạo đối tượng CartResponse
        CartResponse cartResponse = new CartResponse(); // Tạo đối tượng CartResponse
        cartResponse.setId(cart.getId()); // Gán ID giỏ hàng

        // Ánh xạ danh sách Cart_Product sang CartProductResponse
        List<CartProductResponse> cartProductResponses = cart.getCart_products().stream().map(cartProduct -> { // Ánh xạ
                                                                                                               // danh
                                                                                                               // sách
                                                                                                               // Cart_Product
            CartProductResponse cartProductResponse = new CartProductResponse(); // Tạo đối tượng CartProductResponse
            cartProductResponse.setId(cartProduct.getProduct().getId()); // Gán ID sản phẩm
            cartProductResponse.setProductName(cartProduct.getProduct().getName()); // Gán tên sản phẩm
            cartProductResponse.setQuantity(cartProduct.getQuantity()); // Gán số lượng
            cartProductResponse.setPrice(cartProduct.getProduct().getPrice()); // Gán giá sản phẩm
            return cartProductResponse; // Trả về CartProductResponse
        }).collect(Collectors.toList()); // Thu thập kết quả thành danh sách

        cartResponse.setCartProduct(cartProductResponses); // Gán danh sách CartProductResponse vào CartResponse

        // Đặt giá trị total_price vào CartResponse
        cartResponse.setTotal_price(cart.getTotal_price()); // Gán tổng giá vào CartResponse

        return cartResponse; // Trả về CartResponse
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("T(String).valueOf(@cartRepository.findByUserId(#userId).orElseThrow().getUser().getId()) == authentication.principal.claims['sub'] or hasRole('Admin')") // Kiểm
                                                                                                                                                                            // tra
                                                                                                                                                                            // quyền:
                                                                                                                                                                            // chỉ
                                                                                                                                                                            // người
                                                                                                                                                                            // dùng
                                                                                                                                                                            // sở
                                                                                                                                                                            // hữu
                                                                                                                                                                            // giỏ
                                                                                                                                                                            // hàng
                                                                                                                                                                            // hoặc
                                                                                                                                                                            // Admin
                                                                                                                                                                            // được
                                                                                                                                                                            // truy
                                                                                                                                                                            // cập
    public CartResponse getUserIdCart(int userId) { // Phương thức lấy giỏ hàng theo userId
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId); // Tìm Cart theo userId

        CartResponse cartResponse = new CartResponse(); // Tạo đối tượng CartResponse
        if (cartOpt.isPresent()) { // Kiểm tra nếu giỏ hàng tồn tại
            Cart cart = cartOpt.get(); // Lấy Cart
            cartResponse.setId(cart.getId()); // Gán ID giỏ hàng

            List<CartProductResponse> cartProductResponses = cart.getCart_products().stream().map(cartProduct -> { // Ánh
                                                                                                                   // xạ
                                                                                                                   // danh
                                                                                                                   // sách
                                                                                                                   // Cart_Product
                CartProductResponse cartProductResponse = new CartProductResponse(); // Tạo đối tượng
                                                                                     // CartProductResponse
                cartProductResponse.setId(cartProduct.getProduct().getId()); // Gán ID sản phẩm
                cartProductResponse.setProductName(cartProduct.getProduct().getName()); // Gán tên sản phẩm
                cartProductResponse.setQuantity(cartProduct.getQuantity()); // Gán số lượng
                cartProductResponse.setPrice(cartProduct.getProduct().getPrice()); // Gán giá sản phẩm
                return cartProductResponse; // Trả về CartProductResponse
            }).collect(Collectors.toList()); // Thu thập kết quả thành danh sách

            cartResponse.setCartProduct(cartProductResponses); // Gán danh sách CartProductResponse vào CartResponse
            cartResponse.setTotal_price(cart.getTotal_price()); // Gán tổng giá vào CartResponse
        } else { // Nếu không tìm thấy giỏ hàng
            cartResponse.setId(0); // Gán ID bằng 0 (có thể bỏ qua nếu không cần)
            cartResponse.setCartProduct(new ArrayList<>()); // Gán danh sách sản phẩm rỗng
            cartResponse.setTotal_price(0.0); // Gán tổng giá bằng 0
        }

        return cartResponse; // Trả về CartResponse
    }

}