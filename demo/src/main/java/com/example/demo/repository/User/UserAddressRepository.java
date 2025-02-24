package com.example.demo.repository.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.User.UserAddress;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Integer> {
    Optional<List<UserAddress>> findByUserId(int userId);

    @Modifying
    @Transactional
    @Query("update UserAddress ua set ua.defaultAddress = false where ua.user.id = :userId ")
    void clearDefaultAddress(@Param("userId") int userId);

    boolean existsByIdAndUserId(int id, int userId);
}
