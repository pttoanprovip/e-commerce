package com.example.demo.service.Impl.ProductImpl; // Định nghĩa package chứa class này

import java.util.List; // Import lớp List để sử dụng danh sách
import java.util.stream.Collectors; // Import Collectors để thu thập kết quả từ stream

import org.modelmapper.ModelMapper; // Import ModelMapper để ánh xạ giữa DTO và Entity
import org.springframework.security.access.prepost.PreAuthorize; // Import annotation PreAuthorize để kiểm tra quyền trước khi thực thi phương thức
//import org.springframework.beans.factory.annotation.Autowired; // Dòng này bị comment, không sử dụng Autowired
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch

import com.example.demo.dto.req.Product.CategoryRequest; // Import DTO CategoryRequest để nhận dữ liệu đầu vào
import com.example.demo.dto.res.Product.CategoryResponse; // Import DTO CategoryResponse để trả về dữ liệu đầu ra
import com.example.demo.entity.Product.Category; // Import lớp Category từ entity
import com.example.demo.repository.Product.CategoryRepository; // Import CategoryRepository để truy vấn Category
import com.example.demo.service.Product.CategoryService; // Import interface CategoryService mà lớp này triển khai

@Service // Đánh dấu lớp này là một Spring Service
public class CategoryServiceImpl implements CategoryService { // Lớp triển khai interface CategoryService

    private CategoryRepository categoryRepository; // Khai báo biến CategoryRepository để tương tác với cơ sở dữ liệu
                                                   // Category
    private final ModelMapper modelMapper; // Khai báo biến ModelMapper để ánh xạ đối tượng

    // Constructor để tiêm các dependency
    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository; // Gán CategoryRepository được tiêm vào biến instance
        this.modelMapper = modelMapper; // Gán ModelMapper được tiêm vào biến instance
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or hasRole('User')") // Kiểm tra quyền: chỉ Admin hoặc User được truy cập
    public CategoryResponse findById(int id) { // Phương thức tìm danh mục theo ID
        Category category = categoryRepository.findById(id) // Tìm Category theo ID
                .orElseThrow(() -> new RuntimeException("Category not found" + id)); // Ném ngoại lệ nếu không tìm thấy
                                                                                     // danh mục
        return modelMapper.map(category, CategoryResponse.class); // Ánh xạ Category sang CategoryResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được xóa danh mục
    public void delete(int id) { // Phương thức xóa danh mục theo ID
        // Tìm kiếm id nếu sai thì trả về kết quả
        categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found" + id)); // Kiểm tra
                                                                                                            // xem danh
                                                                                                            // mục có
                                                                                                            // tồn tại
                                                                                                            // không
        // Thực hiện hành động xóa
        categoryRepository.deleteById(id); // Xóa danh mục khỏi cơ sở dữ liệu
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or hasRole('User')") // Kiểm tra quyền: chỉ Admin hoặc User được truy cập
    public CategoryResponse findByName(String name) { // Phương thức tìm danh mục theo tên
        Category categorys = categoryRepository.findByName(name) // Tìm Category theo tên
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + name)); // Ném ngoại lệ nếu
                                                                                                   // không tìm thấy
                                                                                                   // danh mục
        return modelMapper.map(categorys, CategoryResponse.class); // Ánh xạ Category sang CategoryResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or hasRole('User')") // Kiểm tra quyền: chỉ Admin hoặc User được truy cập
    public List<CategoryResponse> findAll() { // Phương thức lấy danh sách tất cả danh mục
        List<Category> categorys = categoryRepository.findAll(); // Lấy tất cả Category từ cơ sở dữ liệu
        return categorys.stream().map(category -> modelMapper.map(category, CategoryResponse.class)) // Ánh xạ từng
                                                                                                     // Category sang
                                                                                                     // CategoryResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được thêm danh mục
    public CategoryResponse add(CategoryRequest categoryRequest) { // Phương thức thêm danh mục mới
        // Tạo đối tượng Category từ categoryRequest
        Category category = modelMapper.map(categoryRequest, Category.class); // Ánh xạ CategoryRequest sang Category
                                                                              // entity
        // Lưu vào database
        Category saveCategory = categoryRepository.save(category); // Lưu Category vào cơ sở dữ liệu
        // Ánh xạ từ Category sang categoryResponse
        return modelMapper.map(saveCategory, CategoryResponse.class); // Ánh xạ Category đã lưu sang CategoryResponse và
                                                                      // trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được cập nhật danh mục
    public CategoryResponse update(CategoryRequest categoryRequest, int id) { // Phương thức cập nhật danh mục theo ID
        // Tìm kiếm sản phẩm
        Category categorys = categoryRepository.findById(id) // Tìm Category theo ID
                .orElseThrow(() -> new RuntimeException("Category not found" + id)); // Ném ngoại lệ nếu không tìm thấy
                                                                                     // danh mục
        // Sao chép thuộc tính từ categoryRequest sang category
        modelMapper.map(categoryRequest, categorys); // Ánh xạ thông tin từ CategoryRequest sang Category hiện tại
        // Cập nhật lại
        Category updateCategory = categoryRepository.save(categorys); // Lưu Category đã cập nhật vào cơ sở dữ liệu
        // Trả về response
        return modelMapper.map(updateCategory, CategoryResponse.class); // Ánh xạ Category đã lưu sang CategoryResponse
                                                                        // và trả về
    }

}