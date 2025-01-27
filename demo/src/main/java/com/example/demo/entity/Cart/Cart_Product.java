package com.example.demo.entity.Cart;

import com.example.demo.entity.Product.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "cart_products")
@NoArgsConstructor
@AllArgsConstructor
public class Cart_Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_cart")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "id_product")
    private Product product;

    @Column(name = "quantity")
    private int quantity;
}
