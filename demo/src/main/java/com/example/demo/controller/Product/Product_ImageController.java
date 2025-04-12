package com.example.demo.controller.Product;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.req.Product.ProductImageRequest;
import com.example.demo.dto.res.Product.ProductImageResponse;
import com.example.demo.service.Product.ProductImageService;

@RestController
@RequestMapping("/images")
public class Product_ImageController {
    private ProductImageService productImageService;

    public Product_ImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    // Thêm mới hình ảnh cho sản phẩm
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> add(@RequestParam("file") MultipartFile file,
            @RequestParam("productId") int productId) {
        try {
            System.out.println("Received file: " + file.getOriginalFilename());
            System.out.println("Received productId: " + productId);

            ProductImageRequest request = new ProductImageRequest();
            request.setFile(file);
            request.setProductId(productId);

            ProductImageResponse response = productImageService.add(request, file);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // Cập nhật hình ảnh của sản phẩm theo ID
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestParam ProductImageRequest productImageRequest,
            @PathVariable int id,
            @RequestParam MultipartFile file) {
        try {
            ProductImageResponse productImage = productImageService.update(productImageRequest, id, file);
            return ResponseEntity.ok(productImage);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    // Xóa hình ảnh của sản phẩm theo ID
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