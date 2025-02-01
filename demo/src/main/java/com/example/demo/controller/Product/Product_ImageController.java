package com.example.demo.controller.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.req.Product.ProductImageRequest;
import com.example.demo.dto.res.Product.ProductImageResponse;
import com.example.demo.service.Product.ProductImageService;

@RestController
@RequestMapping("/images")
public class Product_ImageController {
    private ProductImageService productImageService;

    @Autowired
    public Product_ImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody ProductImageRequest productImageRequest) {
        try {
            ProductImageResponse productImage = productImageService.add(productImageRequest);
            return ResponseEntity.ok(productImage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody ProductImageRequest productImageRequest,
            @PathVariable int id) {
        try {
            ProductImageResponse productImage = productImageService.update(productImageRequest, id);
            return ResponseEntity.ok(productImage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            productImageService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
}
