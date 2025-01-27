package com.example.demo.repository.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Order.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem , Integer>{

}
