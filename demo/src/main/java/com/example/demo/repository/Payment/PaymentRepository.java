package com.example.demo.repository.Payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Payment.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer>{
    Optional<Payment> findByTransactionId(String transactionId);
}
