package com.example.demo.repository.Order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Order.Order;
import com.example.demo.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>{
    Optional<List<Order>> findByUserId(int userId);

    @Query("select o from Order o where o.orderStatus = :status")
    List<Order> findByOrderStatus(@Param("status")OrderStatus orderStatus);
}
