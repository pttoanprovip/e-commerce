package com.example.demo.service.Impl.UserImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.req.User.RoleRequest;
import com.example.demo.dto.res.User.RoleResponse;
import com.example.demo.entity.User.Role;
import com.example.demo.repository.User.RoleRepository;
import com.example.demo.service.User.RoleService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    @PreAuthorize("hasRole('Admin')")
    public RoleResponse create(RoleRequest roleRequest) {
        if (roleRepository.existsByRoleName(roleRequest.getRoleName())) {
            throw new RuntimeException("Role already exists");
        }

        Role role = modelMapper.map(roleRequest, Role.class);
        roleRepository.save(role);
        return modelMapper.map(role, RoleResponse.class);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('Admin')")
    public RoleResponse update(int id, RoleRequest roleRequest) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Role not found"));

        if(!role.getRoleName().equals(roleRequest.getRoleName())) {
            throw new RuntimeException("Role already exists");
        }

        modelMapper.map(roleRequest, role);
        roleRepository.save(role);

        return modelMapper.map(role, RoleResponse.class);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('Admin')")
    public void delete(int id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
                roleRepository.delete(role);
    }

    @Override
    @PreAuthorize("hasRole('Admin')")
    public List<RoleResponse> getAll() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(role -> modelMapper.map(role, RoleResponse.class))
                .collect(Collectors.toList());
    }

}
