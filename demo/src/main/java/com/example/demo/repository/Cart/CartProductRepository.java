package com.example.demo.repository.Cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Cart.Cart_Product;

@Repository
public interface CartProductRepository extends JpaRepository<Cart_Product, Integer>{

}
