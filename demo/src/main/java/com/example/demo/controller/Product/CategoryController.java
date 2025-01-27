package com.example.demo.controller.Product;

import java.util.List;

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

import com.example.demo.dto.req.Product.CategoryRequest;
import com.example.demo.dto.res.Product.CategoryResponse;
import com.example.demo.service.Product.CategoryService;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        try {
            List<CategoryResponse> category = categoryService.findAll();
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.status(505).build();
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable int id) {
        try {
            CategoryResponse category = categoryService.findById(id);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<CategoryResponse> getByName(@RequestParam String name){
        try {
            CategoryResponse category = categoryService.findByName(name);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> add(@RequestBody CategoryRequest categoryRequest){
        try {
            CategoryResponse category = categoryService.add(categoryRequest);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@RequestBody CategoryRequest categoryRequest,
                                                   @PathVariable int id){
        try {
            CategoryResponse category = categoryService.update(categoryRequest, id);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }     
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id){
        try {
            categoryService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
}
