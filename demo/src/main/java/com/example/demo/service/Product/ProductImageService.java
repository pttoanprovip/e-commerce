    package com.example.demo.service.Product;

    import org.springframework.web.multipart.MultipartFile;

    import com.example.demo.dto.req.Product.ProductImageRequest;
    import com.example.demo.dto.res.Product.ProductImageResponse;

    public interface ProductImageService {
        ProductImageResponse findById(int id);

        void delete(int id);

        ProductImageResponse add(ProductImageRequest productImageRequest, MultipartFile file);

        ProductImageResponse update(ProductImageRequest productImageRequest, int id, MultipartFile file);
    }
