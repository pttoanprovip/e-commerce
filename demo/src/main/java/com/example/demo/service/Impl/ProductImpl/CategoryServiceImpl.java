package com.example.demo.service.Impl.ProductImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.req.Product.CategoryRequest;
import com.example.demo.dto.res.Product.CategoryResponse;
import com.example.demo.entity.Product.Category;
import com.example.demo.repository.Product.CategoryRepository;
import com.example.demo.service.Product.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    private CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CategoryResponse findById(int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found" + id));
        return modelMapper.map(category, CategoryResponse.class);
    }

    @Override
    @Transactional
    public void delete(int id) {
        // tìm kiếm id nếu sai thì trả về kết quả
        categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found" + id));
        // thực hiện hành động xóa
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryResponse findByName(String name) {
        Category categorys = categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + name));
        return modelMapper.map(categorys, CategoryResponse.class);
    }

    @Override
    public List<CategoryResponse> findAll() {
        List<Category> categorys = categoryRepository.findAll();
        return categorys.stream().map(category -> modelMapper.map(category, CategoryResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse add(CategoryRequest categoryRequest) {
        // Tạo đối tượng Category từ categoryRequest
        Category category = modelMapper.map(categoryRequest, Category.class);
        // Lưu vào database
        Category saveCategory = categoryRepository.save(category);
        // Ánh xạ từ Category sang categoryResponse
        return modelMapper.map(saveCategory, CategoryResponse.class);
    }

    @Override
    @Transactional
    public CategoryResponse update(CategoryRequest categoryRequest, int id) {
        // tìm kiếm sản phẩm
        Category categorys = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found" + id));
        // sao chép thuộc tính từ categoryRequest sang category
        modelMapper.map(categoryRequest, categorys);
        // cập nhật lại
        Category updateCategory = categoryRepository.save(categorys);
        // trả về response
        return modelMapper.map(updateCategory, CategoryResponse.class);
    }

}
