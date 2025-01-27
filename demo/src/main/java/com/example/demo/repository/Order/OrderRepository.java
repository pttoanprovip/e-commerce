package com.example.demo.repository.Order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Order.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>{
    Optional<List<Order>> findByUserId(int userId);
}
