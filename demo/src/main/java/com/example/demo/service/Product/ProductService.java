package com.example.demo.service.Product;

import java.util.List;

import com.example.demo.dto.req.Product.ProductRequest;
import com.example.demo.dto.res.Product.ProductResponse;
import com.example.demo.entity.Product.Product;

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

    List<ProductResponse> findByCriteria(String name, String category, String brand, String cpu, String ram,
            String storage, String screenSize, double price, double minPrice, double maxPrice);

    List<ProductResponse> findByCpu(String cpu);

    List<ProductResponse> findByRam(String ram);

    List<ProductResponse> findByStorage(String storage);

    List<ProductResponse> findByScreenSize(String screenSize);

    List<ProductResponse> findByPrice(double price);

    List<ProductResponse> findByPriceBetween(double minPrice, double maxPrice);

    List<ProductResponse> compareProducts(List<Integer> id);

}
