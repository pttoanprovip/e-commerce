package com.example.demo.dto.res.Product;

import java.util.List;

import lombok.Data;

@Data
public class ProductResponse {
    private int id;
    private String name;
    private String detail;
    private String cpu;
    private String ram;
    private String storage;
    private String screen_size;
    private String brand;
    private double price;

    private CategoryResponse category;
    private List<ProductImageResponse> productImages;
}   