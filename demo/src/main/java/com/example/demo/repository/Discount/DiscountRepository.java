package com.example.demo.repository.Discount;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.discount.Discount;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer>{
    Optional<Discount> findByCode(String code);
}
