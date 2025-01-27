package com.example.demo.service.Product;

import java.util.List;

import com.example.demo.dto.req.Product.ProductRequest;
import com.example.demo.dto.res.Product.ProductResponse;

public interface ProductService {
    List<ProductResponse> findAll();

    ProductResponse findbyId(int id);

    void delete(int id);

    ProductResponse add(ProductRequest productRequest);

    ProductResponse update(ProductRequest productRequest, int id);

    List<ProductResponse> findByName(String name);

    List<ProductResponse> findByCategory(String category);

    List<ProductResponse> findByBrand(String brand);

    List<ProductResponse> findByCategoryAndBrand(String category, String brand);

    List<ProductResponse> findByNameAndBrand(String name, String brand);      
    
    List<ProductResponse> findByCriteria(String name, String category, String brand);
}
