package com.example.demo.service.Impl.UserImpl; // Định nghĩa package chứa class này

import java.util.List; // Import lớp List để sử dụng danh sách
import java.util.stream.Collectors; // Import Collectors để thu thập kết quả từ stream

import org.modelmapper.ModelMapper; // Import ModelMapper để ánh xạ giữa DTO và Entity
import org.springframework.security.access.prepost.PreAuthorize; // Import annotation PreAuthorize để kiểm tra quyền trước khi thực thi phương thức
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch

import com.example.demo.dto.req.User.RoleRequest; // Import DTO RoleRequest để nhận dữ liệu đầu vào
import com.example.demo.dto.res.User.RoleResponse; // Import DTO RoleResponse để trả về dữ liệu đầu ra
import com.example.demo.entity.User.Role; // Import lớp Role từ entity
import com.example.demo.repository.User.RoleRepository; // Import RoleRepository để truy vấn Role
import com.example.demo.service.User.RoleService; // Import interface RoleService mà lớp này triển khai

import lombok.AllArgsConstructor; // Import annotation AllArgsConstructor để tự động tạo constructor với tất cả các field

@Service // Đánh dấu lớp này là một Spring Service
@AllArgsConstructor // Tự động tạo constructor để tiêm tất cả các dependency
public class RoleServiceImpl implements RoleService { // Lớp triển khai interface RoleService

    private final RoleRepository roleRepository; // Khai báo biến RoleRepository để tương tác với cơ sở dữ liệu Role
    private final ModelMapper modelMapper; // Khai báo biến ModelMapper để ánh xạ đối tượng

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch, tự động quản lý rollback nếu có
                   // lỗi
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được phép tạo role mới
    public RoleResponse create(RoleRequest roleRequest) { // Phương thức tạo role mới

        if (roleRepository.existsByRoleName(roleRequest.getRoleName())) { // Kiểm tra xem roleName đã tồn tại trong cơ
                                                                          // sở dữ liệu chưa
            throw new RuntimeException("Role already exists"); // Ném ngoại lệ nếu roleName đã tồn tại
        }

        Role role = modelMapper.map(roleRequest, Role.class); // Ánh xạ RoleRequest sang Role entity
        roleRepository.save(role); // Lưu đối tượng Role vào cơ sở dữ liệu
        return modelMapper.map(role, RoleResponse.class); // Ánh xạ Role đã lưu sang RoleResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được phép cập nhật role
    public RoleResponse update(int id, RoleRequest roleRequest) { // Phương thức cập nhật role theo ID

        Role role = roleRepository.findById(id) // Tìm Role theo ID
                .orElseThrow(() -> new RuntimeException("Role not found")); // Ném ngoại lệ nếu không tìm thấy role

        if (!role.getRoleName().equals(roleRequest.getRoleName()) // Kiểm tra xem roleName mới có khác với roleName hiện
                                                                  // tại không
                && roleRepository.existsByRoleName(roleRequest.getRoleName())) { // Và kiểm tra xem roleName mới đã tồn
                                                                                 // tại chưa
            throw new RuntimeException("Role already exists"); // Ném ngoại lệ nếu roleName mới đã tồn tại
        }

        modelMapper.map(roleRequest, role); // Ánh xạ thông tin từ RoleRequest sang Role hiện tại
        roleRepository.save(role); // Lưu Role đã cập nhật vào cơ sở dữ liệu

        return modelMapper.map(role, RoleResponse.class); // Ánh xạ Role đã lưu sang RoleResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được phép xóa role
    public void delete(int id) { // Phương thức xóa role theo ID

        Role role = roleRepository.findById(id) // Tìm Role theo ID
                .orElseThrow(() -> new RuntimeException("Role not found")); // Ném ngoại lệ nếu không tìm thấy role
        roleRepository.delete(role); // Xóa đối tượng Role khỏi cơ sở dữ liệu
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được phép xem danh sách tất cả role
    public List<RoleResponse> getAll() { // Phương thức lấy danh sách tất cả role

        List<Role> roles = roleRepository.findAll(); // Lấy tất cả Role từ cơ sở dữ liệu
        return roles.stream() // Chuyển danh sách Role thành stream
                .map(role -> modelMapper.map(role, RoleResponse.class)) // Ánh xạ từng Role sang RoleResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

}