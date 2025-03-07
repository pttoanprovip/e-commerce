package com.example.demo.service.User;

import java.util.List;

import com.example.demo.dto.req.User.RoleRequest;
import com.example.demo.dto.res.User.RoleResponse;

public interface RoleService {
    RoleResponse create(RoleRequest roleRequest);

    RoleResponse update(int id, RoleRequest roleRequest);

    void delete(int id);

    List<RoleResponse> getAll();
}
