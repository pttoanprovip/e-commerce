package com.example.demo.dto.res.User;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class UserResponse {
    private int id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createAt;
    private String role;
    private List<UserAddressResponse> address;
}
