package com.example.demo.service.Impl.ProductImpl; // Định nghĩa package chứa class này

import java.util.Map; // Import lớp Map để xử lý kết quả trả về từ Cloudinary

import org.modelmapper.ModelMapper; // Import ModelMapper để ánh xạ giữa DTO và Entity
import org.springframework.security.access.prepost.PreAuthorize; // Import annotation PreAuthorize để kiểm tra quyền trước khi thực thi phương thức
//import org.springframework.beans.factory.annotation.Autowired; // Dòng này bị comment, không sử dụng Autowired
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile để xử lý file upload

import com.cloudinary.Cloudinary; // Import Cloudinary để tương tác với dịch vụ lưu trữ ảnh
import com.cloudinary.utils.ObjectUtils; // Import ObjectUtils để cung cấp các tham số mặc định cho Cloudinary
import com.example.demo.dto.req.Product.ProductImageRequest; // Import DTO ProductImageRequest để nhận dữ liệu đầu vào
import com.example.demo.dto.res.Product.ProductImageResponse; // Import DTO ProductImageResponse để trả về dữ liệu đầu ra
import com.example.demo.entity.Product.Product; // Import lớp Product từ entity
import com.example.demo.entity.Product.Product_Image; // Import lớp Product_Image từ entity
import com.example.demo.repository.Product.ProductImageRepository; // Import ProductImageRepository để truy vấn Product_Image
import com.example.demo.repository.Product.ProductRepository; // Import ProductRepository để truy vấn Product
import com.example.demo.service.Product.ProductImageService; // Import interface ProductImageService mà lớp này triển khai

import lombok.RequiredArgsConstructor; // Import annotation RequiredArgsConstructor để tự động tạo constructor với các field final

@Service // Đánh dấu lớp này là một Spring Service
@RequiredArgsConstructor // Tự động tạo constructor để tiêm các dependency final
public class ProductImageServiceImpl implements ProductImageService { // Lớp triển khai interface ProductImageService

    private final ProductImageRepository productImageRepository; // Khai báo biến ProductImageRepository để tương tác
                                                                 // với cơ sở dữ liệu Product_Image
    private final ModelMapper modelMapper; // Khai báo biến ModelMapper để ánh xạ đối tượng
    private final ProductRepository productRepository; // Khai báo biến ProductRepository để tương tác với cơ sở dữ liệu
                                                       // Product
    private final Cloudinary cloudinary; // Khai báo biến Cloudinary để tương tác với dịch vụ lưu trữ ảnh

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or hasRole('User')") // Kiểm tra quyền: chỉ Admin hoặc User được truy cập
    public ProductImageResponse findById(int id) { // Phương thức tìm ảnh sản phẩm theo ID
        Product_Image product_Image = productImageRepository.findById(id) // Tìm Product_Image theo ID
                .orElseThrow(() -> new RuntimeException("ProductImage not found with id: " + id)); // Ném ngoại lệ nếu
                                                                                                   // không tìm thấy ảnh
        return modelMapper.map(product_Image, ProductImageResponse.class); // Ánh xạ Product_Image sang
                                                                           // ProductImageResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được xóa ảnh sản phẩm
    public void delete(int id) { // Phương thức xóa ảnh sản phẩm theo ID
        // Tìm kiếm id nếu sai thì trả về kết quả
        Product_Image productImage = productImageRepository.findById(id) // Tìm Product_Image theo ID
                .orElseThrow(() -> new RuntimeException("ProductImage not found with id: " + id)); // Ném ngoại lệ nếu
                                                                                                   // không tìm thấy ảnh

        String publicId = extractPublicIdFromUrl(productImage.getImageUrl()); // Lấy public_id từ URL ảnh trên
                                                                              // Cloudinary

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap()); // Xóa ảnh khỏi Cloudinary
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi khi xóa
            throw new RuntimeException("Error: " + e.getMessage()); // Ném ngoại lệ với thông báo lỗi
        }

        // Thực hiện hành động xóa
        productImageRepository.deleteById(id); // Xóa Product_Image khỏi cơ sở dữ liệu
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được thêm ảnh sản phẩm
    public ProductImageResponse add(ProductImageRequest productImageRequest, MultipartFile file) { // Phương thức thêm
                                                                                                   // ảnh sản phẩm mới
        Product product = productRepository.findById(productImageRequest.getProductId()) // Tìm Product theo productId
                                                                                         // từ request
                .orElseThrow(() -> new RuntimeException("Product not found")); // Ném ngoại lệ nếu không tìm thấy sản
                                                                               // phẩm

        // Tạo public_id duy nhất
        String publicId = "product_" + productImageRequest.getProductId() + "_" + System.currentTimeMillis(); // Tạo
                                                                                                              // public_id
                                                                                                              // dựa
                                                                                                              // trên
                                                                                                              // productId
                                                                                                              // và thời
                                                                                                              // gian
                                                                                                              // hiện
                                                                                                              // tại

        // Upload ảnh lên Cloudinary
        Map uploadResult; // Khai báo biến để lưu kết quả upload
        try {
            uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap( // Upload file ảnh lên
                                                                                            // Cloudinary
                    "resource_type", "image", // Xác định loại tài nguyên là ảnh
                    "folder", "product_images", // Lưu ảnh vào thư mục product_images
                    "public_id", publicId, // Sử dụng public_id đã tạo
                    "overwrite", true)); // Ghi đè nếu public_id đã tồn tại
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi khi upload
            throw new RuntimeException("Error: " + e.getMessage()); // Ném ngoại lệ với thông báo lỗi
        }

        String imageUrl = uploadResult.get("secure_url").toString(); // Lấy URL an toàn của ảnh từ kết quả upload

        Product_Image productImage = new Product_Image(); // Tạo đối tượng Product_Image mới
        productImage.setImageUrl(imageUrl); // Gán URL ảnh cho Product_Image
        productImage.setProduct(product); // Liên kết ảnh với Product

        Product_Image save = productImageRepository.save(productImage); // Lưu Product_Image vào cơ sở dữ liệu
        return modelMapper.map(save, ProductImageResponse.class); // Ánh xạ Product_Image đã lưu sang
                                                                  // ProductImageResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được cập nhật ảnh sản phẩm
    public ProductImageResponse update(ProductImageRequest productImageRequest, int id, MultipartFile file) { // Phương
                                                                                                              // thức
                                                                                                              // cập
                                                                                                              // nhật
                                                                                                              // ảnh sản
                                                                                                              // phẩm
                                                                                                              // theo ID
        Product_Image productImage = productImageRepository.findById(id) // Tìm Product_Image theo ID
                .orElseThrow(() -> new RuntimeException("Image not found")); // Ném ngoại lệ nếu không tìm thấy ảnh

        Product product = productRepository.findById(productImageRequest.getProductId()) // Tìm Product theo productId
                                                                                         // từ request
                .orElseThrow(() -> new RuntimeException("Product not found")); // Ném ngoại lệ nếu không tìm thấy sản
                                                                               // phẩm

        // Xóa ảnh cũ khỏi Cloudinary
        String oldPublicId = extractPublicIdFromUrl(productImage.getImageUrl()); // Lấy public_id của ảnh cũ từ URL
        try {
            cloudinary.uploader().destroy(oldPublicId, ObjectUtils.emptyMap()); // Xóa ảnh cũ khỏi Cloudinary
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi khi xóa
            throw new RuntimeException("Error: " + e.getMessage()); // Ném ngoại lệ với thông báo lỗi
        }

        // Tạo public_id mới
        String newpublicId = "product_" + productImageRequest.getProductId() + "_" + System.currentTimeMillis(); // Tạo
                                                                                                                 // public_id
                                                                                                                 // mới
                                                                                                                 // dựa
                                                                                                                 // trên
                                                                                                                 // productId
                                                                                                                 // và
                                                                                                                 // thời
                                                                                                                 // gian
                                                                                                                 // hiện
                                                                                                                 // tại

        // Upload ảnh mới
        Map uploadResult; // Khai báo biến để lưu kết quả upload
        try {
            uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap( // Upload file ảnh mới lên
                                                                                            // Cloudinary
                    "resource_type", "image", // Xác định loại tài nguyên là ảnh
                    "folder", "product_images", // Lưu ảnh vào thư mục product_images
                    "public_id", newpublicId, // Sử dụng public_id mới
                    "overwrite", true)); // Ghi đè nếu public_id đã tồn tại
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi khi upload
            throw new RuntimeException("Error: " + e.getMessage()); // Ném ngoại lệ với thông báo lỗi
        }

        String imageUrl = uploadResult.get("secure_url").toString(); // Lấy URL an toàn của ảnh mới từ kết quả upload

        productImage.setImageUrl(imageUrl); // Cập nhật URL ảnh mới cho Product_Image
        productImage.setProduct(product); // Cập nhật liên kết với Product

        Product_Image save = productImageRepository.save(productImage); // Lưu Product_Image đã cập nhật vào cơ sở dữ
                                                                        // liệu
        return modelMapper.map(save, ProductImageResponse.class); // Ánh xạ Product_Image đã lưu sang
                                                                  // ProductImageResponse và trả về
    }

    // Helper method để lấy public_id từ URL Cloudinary
    private String extractPublicIdFromUrl(String url) { // Phương thức trích xuất public_id từ URL ảnh Cloudinary
        String[] parts = url.split("/"); // Tách URL thành các phần bằng dấu /
        String fileName = parts[parts.length - 1]; // Lấy tên file từ phần cuối của URL, ví dụ: product_1_123456789.jpg
        return "product_images/" + fileName.substring(0, fileName.lastIndexOf(".")); // Trả về public_id, ví dụ:
                                                                                     // product_images/product_1_123456789
    }

}