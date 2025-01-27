package com.example.demo.dto.req.Product;

import java.util.List;

import lombok.Data;

@Data
public class CategoryRequest {
    private String name;

    private List<Integer> productId;
}
