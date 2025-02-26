package com.example.demo.service.Impl.ProductImpl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.beans.factory.annotation.Autowired;
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

    // @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
            ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @PreAuthorize("hasRole('Admin') or hasRole('User')")
    public List<ProductResponse> findAll() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('Admin') or hasRole('User')")
    public ProductResponse findbyId(int id) {
        // tìm kiếm id
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found" + id));
        // ánh xạ từ product sang productResponse
        return modelMapper.map(product, ProductResponse.class);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('Admin')")
    public void delete(int id) {
        // tìm kiếm id nếu sai thì trả về kết quả
        productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found" + id));
        // thực hiện hành động xóa
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('Admin')")
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
    @PreAuthorize("hasRole('Admin')")
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
    public List<ProductResponse> findByCpu(String cpu) {
        List<Product> products = productRepository.findByCpu(cpu);
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> findByRam(String ram) {
        List<Product> products = productRepository.findByRam(ram);
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> findByStorage(String storage) {
        List<Product> products = productRepository.findByStorage(storage);
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> findByScreenSize(String screenSize) {
        List<Product> products = productRepository.findByScreenSize(screenSize);
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> findByPrice(double price) {
        List<Product> products = productRepository.findByPrice(price);
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> findByPriceBetween(double minPrice, double maxPrice) {
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('Admin') or hasRole('User')")
    public List<ProductResponse> findByCriteria(String name, String category, String brand, String cpu, String ram,
            String storage, String screenSize, double price, double minPrice, double maxPrice) {
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
            } else if (cpu != null) {
                products = productRepository.findByCpu(cpu);
            } else if (ram != null) {
                products = productRepository.findByRam(ram);
            } else if (storage != null) {
                products = productRepository.findByStorage(storage);
            } else if (screenSize != null) {
                products = productRepository.findByScreenSize(screenSize);
            } else if (price != 0) {
                products = productRepository.findByPrice(price);
            } else if (minPrice != 0 && maxPrice != 0) {
                products = productRepository.findByPriceBetween(minPrice, maxPrice);
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
