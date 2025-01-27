package com.example.demo.dto.req.Product;

import lombok.Data;

@Data
public class ProductImageRequest {
    private String imageUrl;
    private int productId;
}
