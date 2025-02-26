package com.example.demo.dto.req.Product;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ProductImageRequest {
    private MultipartFile file;
    private int productId;
}
