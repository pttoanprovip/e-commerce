package com.example.demo.dto.req.User;

import lombok.Data;

@Data
public class UserAddressRequest {
    private int UserId;
    private String address;
    private String city;
    private String country;
    private String phone;
    private boolean defaultAddress;
}
