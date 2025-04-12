package com.example.demo.service.Impl.UserImpl; // Định nghĩa package chứa class này

import java.util.List; // Import lớp List để sử dụng danh sách
import java.util.stream.Collectors; // Import Collectors để thu thập kết quả từ stream

import org.modelmapper.ModelMapper; // Import ModelMapper để ánh xạ giữa DTO và Entity
import org.springframework.security.access.prepost.PostAuthorize; // Import annotation PostAuthorize để kiểm tra quyền sau khi thực thi phương thức
import org.springframework.security.access.prepost.PreAuthorize; // Import annotation PreAuthorize để kiểm tra quyền trước khi thực thi phương thức
import org.springframework.security.core.Authentication; // Import lớp Authentication để lấy thông tin xác thực
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder để truy cập ngữ cảnh bảo mật
//import org.springframework.beans.factory.annotation.Autowired; // Dòng này bị comment, không sử dụng Autowired
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder để mã hóa mật khẩu
import org.springframework.security.oauth2.jwt.Jwt; // Import lớp Jwt để xử lý token JWT
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken; // Import JwtAuthenticationToken để xác thực JWT
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch

import com.example.demo.dto.req.User.UserRequest; // Import DTO UserRequest để nhận dữ liệu đầu vào
import com.example.demo.dto.res.User.UserResponse; // Import DTO UserResponse để trả về dữ liệu đầu ra
import com.example.demo.entity.User.Role; // Import lớp Role từ entity User
import com.example.demo.entity.User.User; // Import lớp User từ entity
import com.example.demo.repository.User.RoleRepository; // Import RoleRepository để truy vấn Role
import com.example.demo.repository.User.UserRepository; // Import UserRepository để truy vấn User
import com.example.demo.service.User.UserService; // Import interface UserService mà lớp này triển khai

import lombok.extern.slf4j.Slf4j; // Import annotation Slf4j để sử dụng logging

@Service // Đánh dấu lớp này là một Spring Service
@Slf4j // Tự động tạo logger cho lớp
public class UserServiceImpl implements UserService { // Lớp triển khai interface UserService

    private UserRepository userRepository; // Khai báo biến UserRepository để tương tác với cơ sở dữ liệu User
    private RoleRepository roleRepository; // Khai báo biến RoleRepository để tương tác với cơ sở dữ liệu Role
    private final ModelMapper modelMapper; // Khai báo biến ModelMapper để ánh xạ đối tượng
    private PasswordEncoder passwordEncoder; // Khai báo biến PasswordEncoder để mã hóa mật khẩu

    // Constructor để tiêm các dependency
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper,
            RoleRepository roleRepository) {
        this.userRepository = userRepository; // Gán UserRepository được tiêm vào biến instance
        this.roleRepository = roleRepository; // Gán RoleRepository được tiêm vào biến instance
        this.modelMapper = modelMapper; // Gán ModelMapper được tiêm vào biến instance
        this.passwordEncoder = passwordEncoder; // Gán PasswordEncoder được tiêm vào biến instance

        // Cấu hình lại ModelMapper
        modelMapper.getConfiguration().setAmbiguityIgnored(true); // Bỏ qua các xung đột ánh xạ không rõ ràng
        modelMapper.createTypeMap(UserRequest.class, User.class) // Tạo ánh xạ từ UserRequest sang User
                .addMappings(mapper -> {
                    mapper.skip(User::setId); // Bỏ qua trường ID khi ánh xạ để tránh ghi đè
                });
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch, tự động quản lý rollback nếu có
                   // lỗi
    public UserResponse createUser(UserRequest userRequest) { // Phương thức tạo người dùng mới

        // Lấy thông tin người tạo từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Lấy đối tượng
                                                                                                // Authentication từ ngữ
                                                                                                // cảnh bảo mật
        boolean isAdmin = authentication.getAuthorities().stream() // Kiểm tra quyền của người dùng
                .anyMatch(a -> a.getAuthority().equals("ROLE_Admin")); // Kiểm tra xem có quyền ROLE_Admin không

        // Kiểm tra số điện thoại
        if (userRepository.existsByPhone(userRequest.getPhone())) { // Kiểm tra xem số điện thoại đã tồn tại trong DB
                                                                    // chưa
            throw new RuntimeException("Phone already exist"); // Ném ngoại lệ nếu số điện thoại đã tồn tại
        }

        // Kiểm tra địa chỉ email
        if (userRepository.existsByEmail(userRequest.getEmail())) { // Kiểm tra xem email đã tồn tại trong DB chưa
            throw new RuntimeException("Email already exist"); // Ném ngoại lệ nếu email đã tồn tại
        }

        // Xác định role của người dùng
        Role role = isAdmin && userRequest.getRoleId() != null // Nếu là Admin và có roleId trong request
                // Lấy role từ roleId trong request
                ? roleRepository.findById(userRequest.getRoleId()) // Tìm role theo roleId
                        .orElseThrow(() -> new RuntimeException("Role not found")) // Ném ngoại lệ nếu không tìm thấy
                                                                                   // role
                // Nếu không phải Admin hoặc không có roleId, gán role mặc định (ID = 2, thường
                // là "USER")
                : roleRepository.findById(2) // Tìm role với ID = 2
                        .orElseThrow(() -> new RuntimeException("Role not found")); // Ném ngoại lệ nếu không tìm thấy
                                                                                    // role

        // Chuyển đổi dữ liệu từ DTO sang Entity
        User user = modelMapper.map(userRequest, User.class); // Ánh xạ UserRequest sang User
        user.setRole(role); // Gán role cho user
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword())); // Mã hóa mật khẩu và gán vào user

        // Lưu user vào database
        User saveUser = userRepository.save(user); // Lưu đối tượng user vào cơ sở dữ liệu

        // Trả về Response
        return modelMapper.map(saveUser, UserResponse.class); // Ánh xạ user đã lưu sang UserResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @PostAuthorize("#id.toString() == authentication.principal.claims['sub'] or hasRole('Admin')") // Kiểm tra quyền sau
                                                                                                   // khi thực thi: chỉ
                                                                                                   // cho phép user xem
                                                                                                   // thông tin của
                                                                                                   // chính họ hoặc
                                                                                                   // Admin
    public UserResponse findById(int id) { // Phương thức tìm user theo ID
        User user = userRepository.findById(id) // Tìm user theo ID trong cơ sở dữ liệu
                .orElseThrow(() -> new RuntimeException("User not found")); // Ném ngoại lệ nếu không tìm thấy user
        return modelMapper.map(user, UserResponse.class); // Ánh xạ user sang UserResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin')") // Chỉ Admin mới có quyền truy cập phương thức này
    public List<UserResponse> getAll() { // Phương thức lấy danh sách tất cả user
        log.info("In method get Users"); // Ghi log thông báo đang thực thi phương thức
        List<User> users = userRepository.findAll(); // Lấy tất cả user từ cơ sở dữ liệu
        return users.stream() // Chuyển danh sách user thành stream
                .map(user -> modelMapper.map(user, UserResponse.class)) // Ánh xạ từng user sang UserResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @PreAuthorize("#id.toString() == authentication.principal.claims['sub'] or hasRole('Admin')") // Kiểm tra quyền: chỉ
                                                                                                  // user chính họ hoặc
                                                                                                  // Admin được cập nhật
    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    public UserResponse update(int id, UserRequest userRequest) { // Phương thức cập nhật thông tin user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Lấy thông tin xác
                                                                                                // thực
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication; // Ép kiểu sang JwtAuthenticationToken
        Jwt jwt = (Jwt) jwtAuth.getPrincipal(); // Lấy đối tượng JWT từ authentication

        System.out.println("User ID (sub) from token: " + jwt.getClaim("sub")); // In ra ID của user từ token
        System.out.println("User ID from response: " + id); // In ra ID của user từ tham số

        User user = userRepository.findById(id) // Tìm user theo ID
                .orElseThrow(() -> new RuntimeException("User not found")); // Ném ngoại lệ nếu không tìm thấy user

        System.out.println("User ID in DB: " + user.getId()); // In ra ID của user từ cơ sở dữ liệu

        // Kiểm tra và cập nhật số điện thoại mới nếu có thay đổi
        if (userRequest.getPhone() != null && !userRequest.getPhone().isEmpty() // Nếu số điện thoại trong request không
                                                                                // null và không rỗng
                && !userRequest.getPhone().equals(user.getPhone())) { // Và số điện thoại khác với số hiện tại
            if (userRepository.existsByPhone(userRequest.getPhone())) { // Kiểm tra xem số điện thoại mới đã tồn tại
                                                                        // chưa
                throw new RuntimeException("Phone already exists"); // Ném ngoại lệ nếu số điện thoại đã tồn tại
            }
            user.setPhone(userRequest.getPhone()); // Cập nhật số điện thoại mới
        }

        // Kiểm tra và cập nhật email mới nếu có thay đổi
        if (userRequest.getEmail() != null && !userRequest.getEmail().isEmpty() // Nếu email trong request không null và
                                                                                // không rỗng
                && !userRequest.getEmail().equals(user.getEmail())) { // Và email khác với email hiện tại
            if (userRepository.existsByEmail(userRequest.getEmail())) { // Kiểm tra xem email mới đã tồn tại chưa
                throw new RuntimeException("Email already exits"); // Ném ngoại lệ nếu email đã tồn tại
            }
            user.setEmail(userRequest.getEmail()); // Cập nhật email mới
        }

        // Cập nhật tên
        if (userRequest.getName() != null && !userRequest.getName().isEmpty()) { // Nếu tên trong request không null và
                                                                                 // không rỗng
            user.setName(userRequest.getName()); // Cập nhật tên mới
        }

        // Cập nhật role
        if (userRequest.getRoleId() != user.getRole().getId()) { // Nếu roleId trong request khác với role hiện tại
            Role role = roleRepository.findById(userRequest.getRoleId()) // Tìm role theo roleId
                    .orElseThrow(() -> new RuntimeException("Role not found")); // Ném ngoại lệ nếu không tìm thấy role
            user.setRole(role); // Cập nhật role mới
        }

        // Cập nhật mật khẩu
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) { // Nếu mật khẩu trong request
                                                                                         // không null và không rỗng
            user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword())); // Mã hóa và cập nhật mật khẩu mới
        }

        // Lưu vào database
        User savaUser = userRepository.save(user); // Lưu user đã cập nhật vào cơ sở dữ liệu

        // Trả về Response
        return modelMapper.map(savaUser, UserResponse.class); // Ánh xạ user đã lưu sang UserResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Chỉ Admin mới có quyền truy cập phương thức này
    public void delete(int id) { // Phương thức xóa user theo ID
        User user = userRepository.findById(id) // Tìm user theo ID
                .orElseThrow(() -> new RuntimeException("User not found")); // Ném ngoại lệ nếu không tìm thấy user
        userRepository.delete(user); // Xóa user khỏi cơ sở dữ liệu
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or hasRole('User')") // Admin hoặc User đều có quyền truy cập phương thức này
    public UserResponse getMyInfo() { // Phương thức lấy thông tin của user hiện tại
        String userId = SecurityContextHolder.getContext().getAuthentication().getName(); // Lấy ID của user từ ngữ cảnh
                                                                                          // bảo mật
        User uesr = userRepository.findById(Integer.parseInt(userId)) // Tìm user theo ID
                .orElseThrow(() -> new RuntimeException("User not found")); // Ném ngoại lệ nếu không tìm thấy user
        return modelMapper.map(uesr, UserResponse.class); // Ánh xạ user sang UserResponse và trả về
    }
}