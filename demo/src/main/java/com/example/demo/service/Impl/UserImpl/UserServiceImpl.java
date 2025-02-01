package com.example.demo.service.Impl.UserImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.req.User.UserRequest;
import com.example.demo.dto.res.User.UserResponse;
import com.example.demo.entity.User.Role;
import com.example.demo.entity.User.User;
import com.example.demo.repository.User.RoleRepository;
import com.example.demo.repository.User.UserRepository;
import com.example.demo.service.User.UserService;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;

        // Cấu hình lại ModelMapper
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.createTypeMap(UserRequest.class, User.class)
            .addMappings(mapper -> {
                mapper.skip(User::setId);
            });
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        // Kiểm tra số điện thoại
        if (userRepository.existsByPhone(userRequest.getPhone())) {
            throw new RuntimeException("Phone already exist");
        }

        // Kiểm tra địa chỉ email
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email already exist");
        }

        // Kiểm tra role có tồn tại không
        Role role = roleRepository.findById(userRequest.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Chuyển đổi dữ liệu từ dto sang entity
        User user = modelMapper.map(userRequest, User.class);
        System.out.println("User Id aftermapping" + user.getId());
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));

        // Lưu user vào database
        User saveUser = userRepository.save(user);

        // Trả về Response
        return modelMapper.map(saveUser, UserResponse.class);
    }

    @Override
    public UserResponse findById(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public List<UserResponse> getAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> modelMapper.map(user, UserResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse update(int id, UserRequest userRequest) {
        // Kiểm tra người dùng
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra và cập nhật số điện thoại mới nếu có thay đổi
        if (userRequest.getPhone() != null && !userRequest.getPhone().isEmpty()
                && !userRequest.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(userRequest.getPhone())) {
                throw new RuntimeException("Phone already exists");
            }
            user.setPhone(userRequest.getPhone());
        }

        // Kiểm tra và cập nhật email mới nếu có thay đổi
        if (userRequest.getEmail() != null && !userRequest.getEmail().isEmpty()
                && !userRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userRequest.getEmail())) {
                throw new RuntimeException("Email already exits");
            }
            user.setEmail(userRequest.getEmail());
        }

        // cập nhật tên
        if (userRequest.getName() != null && !userRequest.getName().isEmpty()) {
            user.setName(userRequest.getName());
        }

        // cập nhật role
        if (userRequest.getRoleId() != user.getRole().getId()) {
            Role role = roleRepository.findById(userRequest.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        }

        // cập nhật mật khẩu
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        }

        // Lưu vào database
        User savaUser = userRepository.save(user);

        // Trả về Response
        return modelMapper.map(savaUser, UserResponse.class);

    }

    @Override
    @Transactional
    public void delete(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }
}
