package com.example.demo.repository.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    // @Query("select u from User u left join fetch u.address where u.id = :id")
    // Optional<User> findByIdWithAddress(Integer id);
}
