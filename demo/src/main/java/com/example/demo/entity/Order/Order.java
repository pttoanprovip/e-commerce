package com.example.demo.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.entity.User.User;
import com.example.demo.enums.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name ="id_user")
    private User user;

    @Column(name = "total_price")
    private double total_price;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.Pending;

    @Column(name = "create_at")
    private LocalDateTime create_at = LocalDateTime.now();

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;
}