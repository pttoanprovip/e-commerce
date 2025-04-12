package com.example.demo.controller.Product;

import java.util.List;

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

import com.example.demo.dto.req.Product.CategoryRequest;
import com.example.demo.dto.res.Product.CategoryResponse;
import com.example.demo.service.Product.CategoryService;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Lấy danh sách tất cả danh mục
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<CategoryResponse> category = categoryService.findAll();
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(505).build();
        }
    }

    // Lấy thông tin danh mục theo ID
    @GetMapping("{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        try {
            CategoryResponse category = categoryService.findById(id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // Tìm kiếm danh mục theo tên
    @GetMapping("/search")
    public ResponseEntity<?> getByName(@RequestParam String name) {
        try {
            CategoryResponse category = categoryService.findByName(name);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Thêm mới một danh mục
    @PostMapping
    public ResponseEntity<?> add(@RequestBody CategoryRequest categoryRequest) {
        try {
            CategoryResponse category = categoryService.add(categoryRequest);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Cập nhật thông tin danh mục theo ID
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody CategoryRequest categoryRequest,
            @PathVariable int id) {
        try {
            CategoryResponse category = categoryService.update(categoryRequest, id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Xóa danh mục theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            categoryService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
}