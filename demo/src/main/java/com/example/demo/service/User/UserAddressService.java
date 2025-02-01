package com.example.demo.service.User;

import java.util.List;

import com.example.demo.dto.req.User.UserAddressRequest;
import com.example.demo.dto.res.User.UserAddressResponse;

public interface UserAddressService {
    UserAddressResponse create(UserAddressRequest userAddressRequest);

    void delete(int id);

    List<UserAddressResponse> getUserAddressByUserId(int userId);

    UserAddressResponse update(int id,UserAddressRequest userAddressRequest);

    UserAddressResponse getUserAddressById(int id);
}
