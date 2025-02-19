package com.example.demo.service.User;

import java.util.List;

import com.example.demo.dto.req.User.UserRequest;
import com.example.demo.dto.res.User.UserResponse;

public interface UserService {
    UserResponse createUser(UserRequest userRequest);

    UserResponse findById(int id);

    List<UserResponse> getAll();

    UserResponse update(int id, UserRequest userRequest);

    void delete(int id);

    UserResponse getMyInfo();
}
