package com.example.demo.repository.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer>{

}
