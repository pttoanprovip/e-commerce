package com.example.demo.dto.req.Product;

import com.example.demo.entity.Product.Category;


import lombok.Data;

@Data
public class ProductRequest {
    private String name;
    private String detail;
    private String cpu;
    private String ram;
    private String storage;
    private String screenSize;
    private String brand;
    private Double price;
    private Integer weight;

    private Category Category;
}
