package com.example.demo.controller.User;

import java.util.List;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.req.User.UserAddressRequest;
import com.example.demo.dto.res.User.UserAddressResponse;
import com.example.demo.service.User.UserAddressService;

@RestController
@RequestMapping("/users/addresses")
public class UserAddressController {

    private UserAddressService userAddressService;

    //@Autowired
    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    // Tạo mới một địa chỉ cho người dùng
    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserAddressRequest userAddressRequest) {
        try {
            UserAddressResponse address = userAddressService.create(userAddressRequest);
            return ResponseEntity.ok(address);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // Xóa địa chỉ theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            userAddressService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // Lấy danh sách địa chỉ của người dùng theo ID người dùng
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserAddressByUserId(@PathVariable int userId) {
        try {
            List<UserAddressResponse> address = userAddressService.getUserAddressByUserId(userId);
            return ResponseEntity.ok(address);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // Cập nhật thông tin địa chỉ theo ID
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable int id,
            @RequestBody UserAddressRequest userAddressRequest) {
        try {
            UserAddressResponse address = userAddressService.update(id, userAddressRequest);
            return ResponseEntity.ok(address);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // Lấy thông tin địa chỉ theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserAddressById(@PathVariable int id){
        try {
            UserAddressResponse address = userAddressService.getUserAddressById(id);
            return ResponseEntity.ok(address);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }   
}