package com.example.demo.dto.req.User;

import lombok.Data;

@Data
public class UserAddressRequest {
    private int userId;
    private String address;
    private String ward;
    private String district;
    private String city;
    private String country;
    private String phone;
    private boolean defaultAddress;
}
