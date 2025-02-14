package com.example.demo.repository.Statistic;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Statistic.Statistic;

@Repository
public interface StatisticRepository extends JpaRepository<Statistic, Integer> {
    Optional<Statistic> findByStartDateAndEndDate(LocalDateTime startDate, LocalDateTime endDate);

    @Query("select count(0) from Order o where o.createAt between :startDate and :endDate")
    int countOrderByDay(LocalDateTime startDate, LocalDateTime endDate);

    // @Query("select count(u) from User u where LOWER(u.role.roleName) = LOWER('USER') and u.createAt between :startDate and :endDate")
    // int countUserByDay(LocalDateTime startDate, LocalDateTime endDate);

    @Query("select sum(o.total_price) from Order o where o.createAt between :startDate and :endDate")
    double sumRevenueByDay(LocalDateTime startDate, LocalDateTime endDate);

    @Query("select sum(oi.quantity) from OrderItem oi join oi.order o where o.createAt between :startDate and :endDate")
    int sumProductsSoldByDay(LocalDateTime startDate, LocalDateTime endDate);
}
