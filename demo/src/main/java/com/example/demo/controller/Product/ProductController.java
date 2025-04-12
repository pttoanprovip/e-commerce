package com.example.demo.controller.Product;

import java.util.List;

import org.slf4j.LoggerFactory;
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

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Lấy danh sách tất cả sản phẩm
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<ProductResponse> product = productService.findAll();
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Lấy thông tin sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        try {
            ProductResponse product = productService.findbyId(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // Thêm mới một sản phẩm
    @PostMapping
    public ResponseEntity<?> add(@RequestBody ProductRequest productRequest) {
        try {
            ProductResponse product = productService.add(productRequest);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Cập nhật thông tin sản phẩm theo ID
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody ProductRequest productRequest, @PathVariable int id) {
        try {
            ProductResponse product = productService.update(productRequest, id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Xóa sản phẩm theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            productService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // Tìm kiếm sản phẩm theo các tiêu chí
    @GetMapping("/search")
    public ResponseEntity<?> getProductByCriteria(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) double price,
            @RequestParam(required = false) String cpu,
            @RequestParam(required = false) String ram,
            @RequestParam(required = false) String storage,
            @RequestParam(required = false) String screenSize,
            @RequestParam(required = false) double minPrice,
            @RequestParam(required = false) double maxPrice) {
        try {
            logger.debug("name: {}, brand: {}, category: {}", name, brand, category);
            List<ProductResponse> products = productService.findByCriteria(name, category, brand, screenSize, cpu, ram,
                    storage, price, minPrice, maxPrice);
            if (products.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching products by criteria", e);
            return ResponseEntity.status(500).build();
        }
    }

    // So sánh các sản phẩm theo danh sách ID
    @GetMapping("/compare")
    public ResponseEntity<?> compareProducts(@RequestParam("ids") List<Integer> id) {
        try {
            List<ProductResponse> products = productService.compareProducts(id);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Lỗi khi so sánh sản phẩm: ", e);
            return ResponseEntity.status(500).build();
        }
    }
}