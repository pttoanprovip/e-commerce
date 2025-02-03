package com.example.demo.repository.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.User.User_Address;

@Repository
public interface UserAddressRepository extends JpaRepository<User_Address, Integer> {
    Optional<List<User_Address>> findByUserId(int userId);

    @Modifying
    @Transactional
    @Query("update User_Address ua set ua.defaultAddress = false where ua.user.id = :userId ")
    void clearDefaultAddress(@Param("userId") int userId);
}
