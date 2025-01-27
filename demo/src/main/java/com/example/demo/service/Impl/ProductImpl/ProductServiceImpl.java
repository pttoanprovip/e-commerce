package com.example.demo.service.Impl.ProductImpl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.req.Product.ProductRequest;
import com.example.demo.dto.res.Product.ProductResponse;
import com.example.demo.entity.Product.Category;
import com.example.demo.entity.Product.Product;
import com.example.demo.repository.Product.CategoryRepository;
import com.example.demo.repository.Product.ProductRepository;
import com.example.demo.service.Product.ProductService;

import org.slf4j.Logger;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
            ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<ProductResponse> findAll() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse findbyId(int id) {
        // tìm kiếm id
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found" + id));
        // ánh xạ từ product sang productResponse
        return modelMapper.map(product, ProductResponse.class);
    }

    @Override
    @Transactional
    public void delete(int id) {
        // tìm kiếm id nếu sai thì trả về kết quả
        productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found" + id));
        // thực hiện hành động xóa
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ProductResponse add(ProductRequest productRequest) {
        // Kiểm tra và tìm kiếm Category
        Category category = categoryRepository.findByName(productRequest.getCategory().getName())
                .orElseGet(() -> {
                    // Kiểm tra và tạo mới Category nếu chưa tồn tại
                    Category newCategory = new Category();
                    newCategory.setName(productRequest.getCategory().getName());
                    return categoryRepository.save(newCategory);
                });
        Product product = modelMapper.map(productRequest, Product.class);
        product.setCategory(category);
        // Lưu Product vào cơ sở dữ liệu
        Product saveProduct = productRepository.save(product);
        // Trả về ProductResponse
        return modelMapper.map(saveProduct, ProductResponse.class);
    }

    @Override
    @Transactional
    public ProductResponse update(ProductRequest productRequest, int id) {
        // Kiểm tra và tìm kiếm Product
        Product exits = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id" + id));
        // Cập nhật thông tin Product
        modelMapper.map(productRequest, exits);
        // Kiểm tra và tìm kiếm Category
        Category category = categoryRepository.findByName(productRequest.getCategory().getName())
                .orElseGet(() -> {
                    // Kiểm tra và tạo mới Category nếu chưa tồn tại
                    Category newCategory = new Category();
                    newCategory.setName(productRequest.getCategory().getName());
                    return categoryRepository.save(newCategory);
                });
        exits.setCategory(category);
        // Lưu Product vào cơ sở dữ liệu
        Product updatedProduct = productRepository.save(exits);
        // Trả về ProductResponse
        return modelMapper.map(updatedProduct, ProductResponse.class);
    }

    @Override
    public List<ProductResponse> findByName(String name) {
        List<Product> products = productRepository.findByName(name);
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> findByCategory(String category) {
        Category categoryEntity = categoryRepository.findByName(category)
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + category));
        return productRepository.findByCategory(categoryEntity).stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> findByBrand(String brand) {
        List<Product> products = productRepository.findByBrand(brand);
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> findByCategoryAndBrand(String category, String brand) {
        Category categoryEntity = categoryRepository.findByName(category)
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + category));
        return productRepository.findByCategoryAndBrand(categoryEntity, brand).stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> findByNameAndBrand(String name, String brand) {
        List<Product> products = productRepository.findByNameAndBrand(name, brand);
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> findByCriteria(String name, String category, String brand) {
        try {
            List<Product> products;
            if (name != null && brand != null) {
                products = productRepository.findByNameAndBrand(name, brand);
            } else if (name != null) {
                products = productRepository.findByName(name);
            } else if (category != null && brand != null) {
                Category categoryEntity = categoryRepository.findByName(category)
                        .orElseThrow(() -> new RuntimeException("Category not found with name: " + category));
                products = productRepository.findByCategoryAndBrand(categoryEntity, brand);
            } else if (category != null) {
                Category categoryEntity = categoryRepository.findByName(category)
                        .orElseThrow(() -> new RuntimeException("Category not found with name: " + category));
                products = productRepository.findByCategory(categoryEntity);
            } else if (brand != null) {
                products = productRepository.findByBrand(brand);
            } else {
                products = productRepository.findAll();
            }
            return products.stream().map(product -> modelMapper.map(product, ProductResponse.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to fetch products by criteria", e);
            return Collections.emptyList();
        }
    }
}
