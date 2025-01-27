package com.example.demo.service.Product;

import java.util.List;

import com.example.demo.dto.req.Product.CategoryRequest;
import com.example.demo.dto.res.Product.CategoryResponse;

public interface CategoryService {
    CategoryResponse findById(int id);

    void delete(int id);

    CategoryResponse findByName(String name);

    List<CategoryResponse> findAll();

    CategoryResponse add(CategoryRequest categoryRequest);

    CategoryResponse update(CategoryRequest categoryRequest, int id);
}
