package com.example.demo.service.Impl.ProductImpl;

import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.req.Product.ProductImageRequest;
import com.example.demo.dto.res.Product.ProductImageResponse;
import com.example.demo.entity.Product.Product;
import com.example.demo.entity.Product.Product_Image;
import com.example.demo.repository.Product.ProductImageRepository;
import com.example.demo.repository.Product.ProductRepository;
import com.example.demo.service.Product.ProductImageService;

@Service
public class ProductImageServiceImpl implements ProductImageService {

    private ProductImageRepository productImageRepository;
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;

    //@Autowired
    public ProductImageServiceImpl(ProductImageRepository productImageRepository, ProductRepository productRepository,
            ModelMapper modelMapper) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProductImageResponse findById(int id) {
        Product_Image product_Image = productImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductImage not found with id: " + id));
        return modelMapper.map(product_Image, ProductImageResponse.class);
    }

    @Override
    @Transactional
    public void delete(int id) {
        // tìm kiếm id nếu sai thì trả về kết quả
        productImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductImage not found with id: " + id));
        // thực hiện hành động xóa
        productImageRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ProductImageResponse add(ProductImageRequest productImageRequest) {
        Product product = productRepository.findById(productImageRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productImageRequest.getProductId()));

        Product_Image productImage = new Product_Image();
        productImage.setImageUrl(productImageRequest.getImageUrl());
        productImage.setProduct(product);

        Product_Image savedProductImage = productImageRepository.save(productImage);
        return modelMapper.map(savedProductImage, ProductImageResponse.class);
    }

    @Override
    @Transactional
    public ProductImageResponse update(ProductImageRequest productImageRequest, int id) {
        Product_Image productImage = productImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductImage not found with id: " + id));

        Product product = productRepository.findById(productImageRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productImageRequest.getProductId()));

        productImage.setImageUrl(productImageRequest.getImageUrl());
        productImage.setProduct(product);

        Product_Image updatedProductImage = productImageRepository.save(productImage);
        return modelMapper.map(updatedProductImage, ProductImageResponse.class);
    }
}