package com.example.demo.service.Impl.UserImpl; // Định nghĩa package chứa class này

import java.util.List; // Import lớp List để sử dụng danh sách
import java.util.stream.Collectors; // Import Collectors để thu thập kết quả từ stream

import org.modelmapper.ModelMapper; // Import ModelMapper để ánh xạ giữa DTO và Entity
import org.springframework.security.access.prepost.PreAuthorize; // Import annotation PreAuthorize để kiểm tra quyền trước khi thực thi phương thức
import org.springframework.security.core.Authentication; // Import lớp Authentication để lấy thông tin xác thực
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder để truy cập ngữ cảnh bảo mật
import org.springframework.security.oauth2.jwt.Jwt; // Import lớp Jwt để xử lý token JWT
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken; // Import JwtAuthenticationToken để xác thực JWT
//import org.springframework.beans.factory.annotation.Autowired; // Dòng này bị comment, không sử dụng Autowired
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch

import com.example.demo.dto.req.User.UserAddressRequest; // Import DTO UserAddressRequest để nhận dữ liệu đầu vào
import com.example.demo.dto.res.User.UserAddressResponse; // Import DTO UserAddressResponse để trả về dữ liệu đầu ra
import com.example.demo.entity.User.User; // Import lớp User từ entity
import com.example.demo.entity.User.UserAddress; // Import lớp UserAddress từ entity
import com.example.demo.repository.User.UserAddressRepository; // Import UserAddressRepository để truy vấn UserAddress
import com.example.demo.repository.User.UserRepository; // Import UserRepository để truy vấn User
import com.example.demo.service.User.UserAddressService; // Import interface UserAddressService mà lớp này triển khai

@Service("UserAddressServiceImpl") // Đánh dấu lớp này là một Spring Service với tên bean cụ thể
public class UserAddressServiceImpl implements UserAddressService { // Lớp triển khai interface UserAddressService

    private UserRepository userRepository; // Khai báo biến UserRepository để tương tác với cơ sở dữ liệu User
    private UserAddressRepository userAddressRepository; // Khai báo biến UserAddressRepository để tương tác với cơ sở
                                                         // dữ liệu UserAddress
    private final ModelMapper modelMapper; // Khai báo biến ModelMapper để ánh xạ đối tượng

    // Constructor để tiêm các dependency
    public UserAddressServiceImpl(UserAddressRepository userAddressRepository, UserRepository userRepository,
            ModelMapper modelMapper) {
        this.userAddressRepository = userAddressRepository; // Gán UserAddressRepository được tiêm vào biến instance
        this.modelMapper = modelMapper; // Gán ModelMapper được tiêm vào biến instance
        this.userRepository = userRepository; // Gán UserRepository được tiêm vào biến instance

        modelMapper.getConfiguration().setAmbiguityIgnored(true); // Bỏ qua các xung đột ánh xạ không rõ ràng
        modelMapper.createTypeMap(UserAddressRequest.class, UserAddress.class) // Tạo ánh xạ từ UserAddressRequest sang
                                                                               // UserAddress
                .addMappings(mapper -> {
                    mapper.skip(UserAddress::setId); // Bỏ qua trường ID khi ánh xạ để tránh ghi đè
                });
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch, tự động quản lý rollback nếu có
                   // lỗi
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(#userAddressRequest.userId) == authentication.principal.claims['sub']") // Kiểm
                                                                                                                                 // tra
                                                                                                                                 // quyền:
                                                                                                                                 // chỉ
                                                                                                                                 // Admin
                                                                                                                                 // hoặc
                                                                                                                                 // người
                                                                                                                                 // dùng
                                                                                                                                 // sở
                                                                                                                                 // hữu
                                                                                                                                 // userId
                                                                                                                                 // được
                                                                                                                                 // tạo
                                                                                                                                 // địa
                                                                                                                                 // chỉ
    public UserAddressResponse create(UserAddressRequest userAddressRequest) { // Phương thức tạo địa chỉ mới cho người
                                                                               // dùng

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Lấy đối tượng
                                                                                                // Authentication từ ngữ
                                                                                                // cảnh bảo mật
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication; // Ép kiểu sang JwtAuthenticationToken
        Jwt jwt = (Jwt) jwtAuth.getPrincipal(); // Lấy đối tượng JWT từ authentication

        System.out.println("User ID (sub) from token: " + jwt.getClaim("sub")); // In ra ID của user từ token để debug
        System.out.println("User ID from request: " + userAddressRequest.getUserId()); // In ra userId từ request để
                                                                                       // debug

        // Tìm người dùng từ userId trong yêu cầu
        User user = userRepository.findById(userAddressRequest.getUserId()) // Tìm user theo userId
                .orElseThrow(() -> new RuntimeException("User not found")); // Ném ngoại lệ nếu không tìm thấy user

        // Nếu địa chỉ mới là mặc định, xóa các địa chỉ mặc định cũ
        if (userAddressRequest.isDefaultAddress()) { // Kiểm tra xem địa chỉ mới có được đánh dấu là mặc định không
            userAddressRepository.clearDefaultAddress(user.getId()); // Xóa trạng thái mặc định của các địa chỉ khác của
                                                                     // user
        }

        // Chuyển đổi từ DTO sang Entity
        UserAddress address = modelMapper.map(userAddressRequest, UserAddress.class); // Ánh xạ UserAddressRequest sang
                                                                                      // UserAddress
        address.setUser(user); // Liên kết địa chỉ với đối tượng user

        // Lưu địa chỉ vào cơ sở dữ liệu
        UserAddress saveAddress = userAddressRepository.save(address); // Lưu đối tượng UserAddress vào cơ sở dữ liệu

        // Chuyển đổi đối tượng UserAddress thành DTO và trả về
        return modelMapper.map(saveAddress, UserAddressResponse.class); // Ánh xạ UserAddress đã lưu sang
                                                                        // UserAddressResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(#userAddressRequest.userId) == authentication.principal.claims['sub'] ") // Kiểm
                                                                                                                                  // tra
                                                                                                                                  // quyền:
                                                                                                                                  // chỉ
                                                                                                                                  // Admin
                                                                                                                                  // hoặc
                                                                                                                                  // người
                                                                                                                                  // dùng
                                                                                                                                  // sở
                                                                                                                                  // hữu
                                                                                                                                  // userId
                                                                                                                                  // được
                                                                                                                                  // xóa
                                                                                                                                  // địa
                                                                                                                                  // chỉ
    public void delete(int id) { // Phương thức xóa địa chỉ theo ID

        // Tìm địa chỉ theo ID
        UserAddress address = userAddressRepository.findById(id) // Tìm UserAddress theo ID
                .orElseThrow(() -> new RuntimeException("Address not found")); // Ném ngoại lệ nếu không tìm thấy địa
                                                                               // chỉ

        // Xóa địa chỉ
        userAddressRepository.delete(address); // Xóa đối tượng UserAddress khỏi cơ sở dữ liệu
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
                                                                                                              // xem
                                                                                                              // danh
                                                                                                              // sách
                                                                                                              // địa chỉ
    public List<UserAddressResponse> getUserAddressByUserId(int userId) { // Phương thức lấy danh sách địa chỉ của một
                                                                          // user theo userId

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Lấy đối tượng
                                                                                                // Authentication từ ngữ
                                                                                                // cảnh bảo mật
        if (authentication instanceof JwtAuthenticationToken) { // Kiểm tra xem authentication có phải là
                                                                // JwtAuthenticationToken không
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken(); // Lấy token JWT
            System.out.println("JWT Sub: " + jwt.getClaim("sub")); // In ra ID của user từ token để debug
            System.out.println("Requested User ID: " + userId); // In ra userId từ tham số để debug
        }

        // Tìm các địa chỉ của người dùng
        List<UserAddress> addresses = userAddressRepository.findByUserId(userId) // Tìm danh sách UserAddress theo
                                                                                 // userId
                .orElseThrow(() -> new RuntimeException("Address not found")); // Ném ngoại lệ nếu không tìm thấy địa
                                                                               // chỉ

        // Chuyển đổi từ Entity sang DTO và trả về danh sách
        return addresses.stream() // Chuyển danh sách UserAddress thành stream
                .map(address -> modelMapper.map(address, UserAddressResponse.class)) // Ánh xạ từng UserAddress sang
                                                                                     // UserAddressResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(#userAddressRequest.userId) == authentication.principal.claims['sub']") // Kiểm
                                                                                                                                 // tra
                                                                                                                                 // quyền:
                                                                                                                                 // chỉ
                                                                                                                                 // Admin
                                                                                                                                 // hoặc
                                                                                                                                 // người
                                                                                                                                 // dùng
                                                                                                                                 // sở
                                                                                                                                 // hữu
                                                                                                                                 // userId
                                                                                                                                 // được
                                                                                                                                 // cập
                                                                                                                                 // nhật
                                                                                                                                 // địa
                                                                                                                                 // chỉ
    public UserAddressResponse update(int id, UserAddressRequest userAddressRequest) { // Phương thức cập nhật địa chỉ
                                                                                       // theo ID

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Lấy đối tượng
                                                                                                // Authentication từ ngữ
                                                                                                // cảnh bảo mật
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication; // Ép kiểu sang JwtAuthenticationToken
        Jwt jwt = (Jwt) jwtAuth.getPrincipal(); // Lấy đối tượng JWT từ authentication

        System.out.println("User ID (sub) from token: " + jwt.getClaim("sub")); // In ra ID của user từ token để debug
        System.out.println("User ID from request: " + userAddressRequest.getUserId()); // In ra userId từ request để
                                                                                       // debug

        // Tìm địa chỉ theo ID
        UserAddress address = userAddressRepository.findById(id) // Tìm UserAddress theo ID
                .orElseThrow(() -> new RuntimeException("Address not found")); // Ném ngoại lệ nếu không tìm thấy địa
                                                                               // chỉ

        // Debug: In thông tin chủ sở hữu địa chỉ
        System.out.println("Address Owner ID: " + address.getUser().getId()); // In ra ID của user sở hữu địa chỉ để
                                                                              // debug

        // Nếu yêu cầu là địa chỉ mặc định, cần xóa mặc định cho các địa chỉ khác của
        // người dùng
        if (userAddressRequest.isDefaultAddress()) { // Kiểm tra xem địa chỉ được cập nhật có được đánh dấu là mặc định
                                                     // không
            // Xóa tất cả các địa chỉ mặc định khác của người dùng
            userAddressRepository.clearDefaultAddress(address.getUser().getId()); // Xóa trạng thái mặc định của các địa
                                                                                  // chỉ khác của user
        }

        // Cập nhật thông tin địa chỉ
        modelMapper.map(userAddressRequest, address); // Ánh xạ thông tin từ UserAddressRequest sang UserAddress hiện
                                                      // tại

        // Lưu và trả về địa chỉ đã được cập nhật
        UserAddress updatedAddress = userAddressRepository.save(address); // Lưu UserAddress đã cập nhật vào cơ sở dữ
                                                                          // liệu
        return modelMapper.map(updatedAddress, UserAddressResponse.class); // Ánh xạ UserAddress đã lưu sang
                                                                           // UserAddressResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(@userAddressRepository.findById(#id).orElseThrow().getUser().getId()) == authentication.principal.claims['sub']") // Kiểm
                                                                                                                                                                           // tra
                                                                                                                                                                           // quyền:
                                                                                                                                                                           // chỉ
                                                                                                                                                                           // Admin
                                                                                                                                                                           // hoặc
                                                                                                                                                                           // người
                                                                                                                                                                           // dùng
                                                                                                                                                                           // sở
                                                                                                                                                                           // hữu
                                                                                                                                                                           // địa
                                                                                                                                                                           // chỉ
                                                                                                                                                                           // được
                                                                                                                                                                           // xem
                                                                                                                                                                           // chi
                                                                                                                                                                           // tiết
    public UserAddressResponse getUserAddressById(int id) { // Phương thức lấy chi tiết địa chỉ theo ID

        // Tìm địa chỉ theo ID
        UserAddress address = userAddressRepository.findById(id) // Tìm UserAddress theo ID
                .orElseThrow(() -> new RuntimeException("Address not found")); // Ném ngoại lệ nếu không tìm thấy địa
                                                                               // chỉ

        // Chuyển đổi Entity sang DTO và trả về
        return modelMapper.map(address, UserAddressResponse.class); // Ánh xạ UserAddress sang UserAddressResponse và
                                                                    // trả về
    }
}