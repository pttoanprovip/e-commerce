package com.example.demo.dto.req.User;

import lombok.Data;

@Data
public class UserRequest {
    private String name;
    private String password;
    private String email;
    private String phone;
    private int RoleId;
}
