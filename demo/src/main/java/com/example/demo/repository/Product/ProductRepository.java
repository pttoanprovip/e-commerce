package com.example.demo.repository.Product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.res.Product.ProductResponse;
import com.example.demo.entity.Product.Category;
import com.example.demo.entity.Product.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByName(String name);

    List<Product> findByCategory(Category category);

    List<Product> findByBrand(String brand);

    List<Product> findByCategoryAndBrand(Category category, String brand);

    List<Product> findByNameAndBrand(String name, String brand);

    List<Product> findByCpu(String cpu);

    List<Product> findByRam(String ram);

    List<Product> findByStorage(String storage);

    List<Product> findByScreenSize(String screenSize);

    List<Product> findByPrice(double price);

    List<Product> findByPriceBetween(double minPrice, double maxPrice);
}
