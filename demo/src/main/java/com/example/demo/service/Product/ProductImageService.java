package com.example.demo.service.Product;

import com.example.demo.dto.req.Product.ProductImageRequest;
import com.example.demo.dto.res.Product.ProductImageResponse;

public interface ProductImageService {
    ProductImageResponse findById(int id);

    void delete(int id);

    ProductImageResponse add(ProductImageRequest productImageRequest);

    ProductImageResponse update(ProductImageRequest productImageRequest, int id);
}
