package com.example.demo.controller.User;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.req.User.UserRequest;
import com.example.demo.dto.res.User.UserResponse;
import com.example.demo.service.User.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private UserService userService;

    // Constructor injection để tiêm UserService
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // API tạo mới người dùng
    @PostMapping
    public ResponseEntity<?> add(@RequestBody UserRequest userRequest) {
        try {
            UserResponse user = userService.createUser(userRequest);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // lỗi nghiệp vụ
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error"); // lỗi hệ thống
        }
    }

    // API lấy thông tin người dùng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable int id) {
        try {
            UserResponse user = userService.findById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // API lấy danh sách tất cả người dùng
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            // Lấy thông tin người dùng hiện tại từ context bảo mật
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            log.info("Username: {}", authentication.getName()); // log username hiện tại
            authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority())); // log
                                                                                                                    // các
                                                                                                                    // quyền

            List<UserResponse> user = userService.getAll();
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // API cập nhật người dùng theo ID
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable int id,
            @RequestBody UserRequest userRequest) {
        try {
            UserResponse user = userService.update(id, userRequest);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // API xóa người dùng theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            userService.delete(id);
            return ResponseEntity.noContent().build(); // trả về 204 nếu xóa thành công
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // API lấy thông tin người dùng đang đăng nhập
    @GetMapping("/myInfo")
    public ResponseEntity<?> getMyInfo() {
        try {
            UserResponse user = userService.getMyInfo();
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}
