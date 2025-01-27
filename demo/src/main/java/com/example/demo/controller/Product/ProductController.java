package com.example.demo.controller.Product;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.req.Product.ProductRequest;
import com.example.demo.dto.res.Product.ProductResponse;
import com.example.demo.service.Product.ProductService;

import org.slf4j.Logger;

@RestController
@RequestMapping("/products")
public class ProductController {
    private ProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        try {
            List<ProductResponse> product = productService.findAll();
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable int id) {
        try {
            ProductResponse product = productService.findbyId(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @PostMapping
    public ResponseEntity<ProductResponse> add(@RequestBody ProductRequest productRequest) {
        try {
            ProductResponse product = productService.add(productRequest);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@RequestBody ProductRequest productRequest, @PathVariable int id) {
        try {
            ProductResponse product = productService.update(productRequest, id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        try {
            productService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> getProductByCriteria(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category, 
            @RequestParam(required = false) String brand) {
        try {
            logger.debug("name: {}, brand: {}, category: {}", name, brand, category);
            List<ProductResponse> products = productService.findByCriteria(name, category, brand);
            if (products.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error fetching products by criteria", e);
            return ResponseEntity.status(500).build();
        }   
    }
}
