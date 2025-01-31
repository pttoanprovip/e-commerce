package com.example.demo.repository.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{
    boolean existByPhone(String phone);
    boolean existByEmail(String email);
}
