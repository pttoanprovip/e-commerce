package com.example.demo.repository.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    Optional<User> findByName(String name);
}
