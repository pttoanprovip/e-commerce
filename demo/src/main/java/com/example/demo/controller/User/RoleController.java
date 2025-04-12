package com.example.demo.controller.User;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.req.User.RoleRequest;
import com.example.demo.dto.res.User.RoleResponse;
import com.example.demo.service.User.RoleService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/roles")
@AllArgsConstructor
public class RoleController {
    private final RoleService roleService;

    // Tạo mới một vai trò
    @PostMapping
    public ResponseEntity<?> create(@RequestBody RoleRequest roleRequest) {
        try {
            RoleResponse role = roleService.create(roleRequest);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // Cập nhật thông tin vai trò theo ID
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody RoleRequest roleRequest) {
        try {
            RoleResponse role = roleService.update(id, roleRequest);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // Xóa vai trò theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            roleService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // Lấy danh sách tất cả vai trò
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<RoleResponse> roles = roleService.getAll();
            return ResponseEntity.ok(roles);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}