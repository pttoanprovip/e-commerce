package com.example.demo.repository.Cart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Cart.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserId(int userId);
}
