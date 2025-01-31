package com.example.demo.repository.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User.User_Address;

@Repository
public interface UserAddressRepository extends JpaRepository<User_Address , Integer>{

}
