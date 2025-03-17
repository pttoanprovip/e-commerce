package com.example.demo.service.Impl.UserImpl;

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

import com.example.demo.dto.req.User.UserAddressRequest;
import com.example.demo.dto.res.User.UserAddressResponse;
import com.example.demo.entity.User.User;
import com.example.demo.entity.User.UserAddress;
import com.example.demo.repository.User.UserAddressRepository;
import com.example.demo.repository.User.UserRepository;
import com.example.demo.service.User.UserAddressService;

@Service("UserAddressServiceImpl")
public class UserAddressServiceImpl implements UserAddressService {

    private UserRepository userRepository;
    private UserAddressRepository userAddressRepository;
    private final ModelMapper modelMapper;

    // @Autowired
    public UserAddressServiceImpl(UserAddressRepository userAddressRepository, UserRepository userRepository,
            ModelMapper modelMapper) {
        this.userAddressRepository = userAddressRepository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;

        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.createTypeMap(UserAddressRequest.class, UserAddress.class)
                .addMappings(mapper -> {
                    mapper.skip(UserAddress::setId);
                });
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(#userAddressRequest.userId) == authentication.principal.claims['sub']")
    public UserAddressResponse create(UserAddressRequest userAddressRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        Jwt jwt = (Jwt) jwtAuth.getPrincipal();

        System.out.println("User ID (sub) from token: " + jwt.getClaim("sub"));
        System.out.println("User ID from request: " + userAddressRequest.getUserId());
        // Tìm người dùng từ userId trong yêu cầu
        User user = userRepository.findById(userAddressRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Nếu địa chỉ mới là mặc định, xóa các địa chỉ mặc định cũ
        if (userAddressRequest.isDefaultAddress()) {
            userAddressRepository.clearDefaultAddress(user.getId());
        }

        // Chuyển đổi từ DTO sang Entity
        UserAddress address = modelMapper.map(userAddressRequest, UserAddress.class);
        address.setUser(user); // Liên kết địa chỉ với người dùng

        // Lưu địa chỉ vào cơ sở dữ liệu
        UserAddress saveAddress = userAddressRepository.save(address);

        // Chuyển đổi đối tượng User_Address thành DTO và trả về
        return modelMapper.map(saveAddress, UserAddressResponse.class);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(#userAddressRequest.userId) == authentication.principal.claims['sub'] ")
    public void delete(int id) {
        // Tìm địa chỉ theo ID
        UserAddress address = userAddressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Xóa địa chỉ
        userAddressRepository.delete(address);
    }

    @Override
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(#userId) == authentication.principal.claims['sub']")
    public List<UserAddressResponse> getUserAddressByUserId(int userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            System.out.println("JWT Sub: " + jwt.getClaim("sub"));
            System.out.println("Requested User ID: " + userId);
        }

        // Tìm các địa chỉ của người dùng
        List<UserAddress> addresses = userAddressRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Chuyển đổi từ Entity sang DTO và trả về danh sách
        return addresses.stream()
                .map(address -> modelMapper.map(address, UserAddressResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(#userAddressRequest.userId) == authentication.principal.claims['sub']")
    public UserAddressResponse update(int id, UserAddressRequest userAddressRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        Jwt jwt = (Jwt) jwtAuth.getPrincipal();

        System.out.println("User ID (sub) from token: " + jwt.getClaim("sub"));
        System.out.println("User ID from request: " + userAddressRequest.getUserId());

        // Tìm địa chỉ theo ID
        UserAddress address = userAddressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Debug: In thông tin chủ sở hữu địa chỉ
        System.out.println("Address Owner ID: " + address.getUser().getId());

        // Nếu yêu cầu là địa chỉ mặc định, cần xóa mặc định cho các địa chỉ khác của
        // người dùng
        if (userAddressRequest.isDefaultAddress()) {
            // Xóa tất cả các địa chỉ mặc định khác của người dùng
            userAddressRepository.clearDefaultAddress(address.getUser().getId());
        }

        // Cập nhật thông tin địa chỉ
        modelMapper.map(userAddressRequest, address);

        // Lưu và trả về địa chỉ đã được cập nhật
        UserAddress updatedAddress = userAddressRepository.save(address);
        return modelMapper.map(updatedAddress, UserAddressResponse.class);
    }

    @Override
    @PreAuthorize("hasRole('Admin') or T(String).valueOf(@userAddressRepository.findById(#id).orElseThrow().getUser().getId()) == authentication.principal.claims['sub']")
    public UserAddressResponse getUserAddressById(int id) {
        // Tìm địa chỉ theo ID
        UserAddress address = userAddressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Chuyển đổi Entity sang DTO và trả về
        return modelMapper.map(address, UserAddressResponse.class);
    }
}
