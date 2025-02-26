package com.example.demo.service.Impl.ProductImpl;

import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.dto.req.Product.ProductImageRequest;
import com.example.demo.dto.res.Product.ProductImageResponse;
import com.example.demo.entity.Product.Product;
import com.example.demo.entity.Product.Product_Image;
import com.example.demo.repository.Product.ProductImageRepository;
import com.example.demo.repository.Product.ProductRepository;
import com.example.demo.service.Product.ProductImageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final Cloudinary cloudinary;

    @Override
    @PreAuthorize("hasRole('Admin') or hasRole('User')")
    public ProductImageResponse findById(int id) {
        Product_Image product_Image = productImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductImage not found with id: " + id));
        return modelMapper.map(product_Image, ProductImageResponse.class);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('Admin')")
    public void delete(int id) {
        // tìm kiếm id nếu sai thì trả về kết quả
        Product_Image productImage = productImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductImage not found with id: " + id));

        String publicId = extractPublicIdFromUrl(productImage.getImageUrl());

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }

        // thực hiện hành động xóa
        productImageRepository.deleteById(id);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('Admin')")
    public ProductImageResponse add(ProductImageRequest productImageRequest, MultipartFile file) {
        Product product = productRepository.findById(productImageRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Tạo public_id duy nhất
        String publicId = "product_" + productImageRequest.getProductId() + "_" + System.currentTimeMillis();

        // Upload ảnh lên Clodinary
        Map uploadResult;
        try {
            uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "image",
                    "folder", "product_images",
                    "public_id", publicId,
                    "overwrite", true));
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }

        String imageUrl = uploadResult.get("secure_url").toString();

        Product_Image productImage = new Product_Image();
        productImage.setImageUrl(imageUrl);
        productImage.setProduct(product);

        Product_Image save = productImageRepository.save(productImage);
        return modelMapper.map(save, ProductImageResponse.class);

    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('Admin')")
    public ProductImageResponse update(ProductImageRequest productImageRequest, int id, MultipartFile file) {
        Product_Image productImage = productImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        Product product = productRepository.findById(productImageRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Xóa ảnh cũ khỏi Cloudinary
        String oldPublicId = extractPublicIdFromUrl(productImage.getImageUrl());
        try {
            cloudinary.uploader().destroy(oldPublicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }

        // Tạo public_id mới
        String newpublicId = "product_" + productImageRequest.getProductId() + "_" + System.currentTimeMillis();

        // Upload ảnh mới
        Map uploadResult;
        try {
            uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "image",
                    "folder", "product_images",
                    "public_id", newpublicId,
                    "overwrite", true));
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }

        String imageUrl = uploadResult.get("secure_url").toString();

        productImage.setImageUrl(imageUrl);
        productImage.setProduct(product);

        Product_Image save = productImageRepository.save(productImage);
        return modelMapper.map(save, ProductImageResponse.class);
    }

    // Helper method để lấy public_id từ URL Cloudinary
    private String extractPublicIdFromUrl(String url) {
        String[] parts = url.split("/");
        String fileName = parts[parts.length - 1]; // Lấy tên file, ví dụ: product_1_123456789.jpg
        return "product_images/" + fileName.substring(0, fileName.lastIndexOf(".")); // Trả về public_id, ví dụ:
                                                                                     // product_images/product_1_123456789
    }

}