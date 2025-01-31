package com.example.demo.dto.res.User;

import lombok.Data;

@Data
public class UserAddressResponse {
    private int id;
    private String address;
    private String city;
    private String country;
    private String phone;
    private boolean defaultAddress;
}
