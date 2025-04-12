package com.example.demo.service.Impl.ProductImpl; // Định nghĩa package chứa class này

import java.util.Collections; // Import lớp Collections để sử dụng danh sách rỗng khi cần
import java.util.List; // Import lớp List để sử dụng danh sách
import java.util.stream.Collectors; // Import Collectors để thu thập kết quả từ stream

import org.modelmapper.ModelMapper; // Import ModelMapper để ánh xạ giữa DTO và Entity
import org.slf4j.LoggerFactory; // Import LoggerFactory để tạo logger
import org.springframework.security.access.prepost.PreAuthorize; // Import annotation PreAuthorize để kiểm tra quyền trước khi thực thi phương thức
//import org.springframework.beans.factory.annotation.Autowired; // Dòng này bị comment, không sử dụng Autowired
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch

import com.example.demo.dto.req.Product.ProductRequest; // Import DTO ProductRequest để nhận dữ liệu đầu vào
import com.example.demo.dto.res.Product.ProductResponse; // Import DTO ProductResponse để trả về dữ liệu đầu ra
import com.example.demo.entity.Product.Category; // Import lớp Category từ entity
import com.example.demo.entity.Product.Product; // Import lớp Product từ entity
import com.example.demo.repository.Product.CategoryRepository; // Import CategoryRepository để truy vấn Category
import com.example.demo.repository.Product.ProductRepository; // Import ProductRepository để truy vấn Product
import com.example.demo.service.Product.ProductService; // Import interface ProductService mà lớp này triển khai

import org.slf4j.Logger; // Import Logger để ghi log

@Service // Đánh dấu lớp này là một Spring Service
public class ProductServiceImpl implements ProductService { // Lớp triển khai interface ProductService

    private final ProductRepository productRepository; // Khai báo biến ProductRepository để tương tác với cơ sở dữ liệu
                                                       // Product
    private final CategoryRepository categoryRepository; // Khai báo biến CategoryRepository để tương tác với cơ sở dữ
                                                         // liệu Category
    private final ModelMapper modelMapper; // Khai báo biến ModelMapper để ánh xạ đối tượng
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class); // Khởi tạo logger cho lớp

    // Constructor để tiêm các dependency
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
            ModelMapper modelMapper) {
        this.productRepository = productRepository; // Gán ProductRepository được tiêm vào biến instance
        this.categoryRepository = categoryRepository; // Gán CategoryRepository được tiêm vào biến instance
        this.modelMapper = modelMapper; // Gán ModelMapper được tiêm vào biến instance
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or hasRole('User')") // Kiểm tra quyền: chỉ Admin hoặc User được truy cập
    public List<ProductResponse> findAll() { // Phương thức lấy danh sách tất cả sản phẩm
        List<Product> products = productRepository.findAll(); // Lấy tất cả Product từ cơ sở dữ liệu
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product
                                                                                                 // sang ProductResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or hasRole('User')") // Kiểm tra quyền: chỉ Admin hoặc User được truy cập
    public ProductResponse findbyId(int id) { // Phương thức tìm sản phẩm theo ID
        // Tìm kiếm id
        Product product = productRepository.findById(id) // Tìm Product theo ID
                .orElseThrow(() -> new RuntimeException("Product not found" + id)); // Ném ngoại lệ nếu không tìm thấy
                                                                                    // sản phẩm
        // Ánh xạ từ product sang productResponse
        return modelMapper.map(product, ProductResponse.class); // Ánh xạ Product sang ProductResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được xóa sản phẩm
    public void delete(int id) { // Phương thức xóa sản phẩm theo ID
        // Tìm kiếm id nếu sai thì trả về kết quả
        productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found" + id)); // Kiểm tra
                                                                                                          // xem sản
                                                                                                          // phẩm có tồn
                                                                                                          // tại không
        // Thực hiện hành động xóa
        productRepository.deleteById(id); // Xóa sản phẩm khỏi cơ sở dữ liệu
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được thêm sản phẩm
    public ProductResponse add(ProductRequest productRequest) { // Phương thức thêm sản phẩm mới
        // Kiểm tra và tìm kiếm Category
        Category category = categoryRepository.findByName(productRequest.getCategory().getName()) // Tìm Category theo
                                                                                                  // tên
                .orElseGet(() -> {
                    // Kiểm tra và tạo mới Category nếu chưa tồn tại
                    Category newCategory = new Category(); // Tạo đối tượng Category mới
                    newCategory.setName(productRequest.getCategory().getName()); // Gán tên Category từ request
                    return categoryRepository.save(newCategory); // Lưu Category mới vào cơ sở dữ liệu
                });
        Product product = modelMapper.map(productRequest, Product.class); // Ánh xạ ProductRequest sang Product entity
        product.setCategory(category); // Liên kết sản phẩm với Category
        // Lưu Product vào cơ sở dữ liệu
        Product saveProduct = productRepository.save(product); // Lưu Product vào cơ sở dữ liệu
        // Trả về ProductResponse
        return modelMapper.map(saveProduct, ProductResponse.class); // Ánh xạ Product đã lưu sang ProductResponse và trả
                                                                    // về
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được cập nhật sản phẩm
    public ProductResponse update(ProductRequest productRequest, int id) { // Phương thức cập nhật sản phẩm theo ID
        // Kiểm tra và tìm kiếm Product
        Product exits = productRepository.findById(id) // Tìm Product theo ID
                .orElseThrow(() -> new RuntimeException("Product not found with id" + id)); // Ném ngoại lệ nếu không
                                                                                            // tìm thấy sản phẩm
        // Cập nhật thông tin Product
        modelMapper.map(productRequest, exits); // Ánh xạ thông tin từ ProductRequest sang Product hiện tại
        // Kiểm tra và tìm kiếm Category
        Category category = categoryRepository.findByName(productRequest.getCategory().getName()) // Tìm Category theo
                                                                                                  // tên
                .orElseGet(() -> {
                    // Kiểm tra và tạo mới Category nếu chưa tồn tại
                    Category newCategory = new Category(); // Tạo đối tượng Category mới
                    newCategory.setName(productRequest.getCategory().getName()); // Gán tên Category từ request
                    return categoryRepository.save(newCategory); // Lưu Category mới vào cơ sở dữ liệu
                });
        exits.setCategory(category); // Liên kết sản phẩm với Category
        // Lưu Product vào cơ sở dữ liệu
        Product updatedProduct = productRepository.save(exits); // Lưu Product đã cập nhật vào cơ sở dữ liệu
        // Trả về ProductResponse
        return modelMapper.map(updatedProduct, ProductResponse.class); // Ánh xạ Product đã lưu sang ProductResponse và
                                                                       // trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ProductResponse> findByName(String name) { // Phương thức tìm sản phẩm theo tên
        List<Product> products = productRepository.findByName(name); // Tìm danh sách Product theo tên
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product
                                                                                                 // sang ProductResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ProductResponse> findByCategory(String category) { // Phương thức tìm sản phẩm theo danh mục
        Category categoryEntity = categoryRepository.findByName(category) // Tìm Category theo tên
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + category)); // Ném ngoại lệ
                                                                                                       // nếu không tìm
                                                                                                       // thấy danh mục
        return productRepository.findByCategory(categoryEntity).stream() // Tìm danh sách Product theo Category
                .map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product sang
                                                                                 // ProductResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ProductResponse> findByBrand(String brand) { // Phương thức tìm sản phẩm theo thương hiệu
        List<Product> products = productRepository.findByBrand(brand); // Tìm danh sách Product theo thương hiệu
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product
                                                                                                 // sang ProductResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ProductResponse> findByCategoryAndBrand(String category, String brand) { // Phương thức tìm sản phẩm
                                                                                         // theo danh mục và thương hiệu
        Category categoryEntity = categoryRepository.findByName(category) // Tìm Category theo tên
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + category)); // Ném ngoại lệ
                                                                                                       // nếu không tìm
                                                                                                       // thấy danh mục
        return productRepository.findByCategoryAndBrand(categoryEntity, brand).stream() // Tìm danh sách Product theo
                                                                                        // Category và thương hiệu
                .map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product sang
                                                                                 // ProductResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ProductResponse> findByNameAndBrand(String name, String brand) { // Phương thức tìm sản phẩm theo tên và
                                                                                 // thương hiệu
        List<Product> products = productRepository.findByNameAndBrand(name, brand); // Tìm danh sách Product theo tên và
                                                                                    // thương hiệu
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product
                                                                                                 // sang ProductResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ProductResponse> findByCpu(String cpu) { // Phương thức tìm sản phẩm theo CPU
        List<Product> products = productRepository.findByCpu(cpu); // Tìm danh sách Product theo CPU
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product
                                                                                                 // sang ProductResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ProductResponse> findByRam(String ram) { // Phương thức tìm sản phẩm theo RAM
        List<Product> products = productRepository.findByRam(ram); // Tìm danh sách Product theo RAM
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product
                                                                                                 // sang ProductResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ProductResponse> findByStorage(String storage) { // Phương thức tìm sản phẩm theo bộ nhớ
        List<Product> products = productRepository.findByStorage(storage); // Tìm danh sách Product theo bộ nhớ
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product
                                                                                                 // sang ProductResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ProductResponse> findByScreenSize(String screenSize) { // Phương thức tìm sản phẩm theo kích thước màn
                                                                       // hình
        List<Product> products = productRepository.findByScreenSize(screenSize); // Tìm danh sách Product theo kích
                                                                                 // thước màn hình
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product
                                                                                                 // sang ProductResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ProductResponse> findByPrice(double price) { // Phương thức tìm sản phẩm theo giá
        List<Product> products = productRepository.findByPrice(price); // Tìm danh sách Product theo giá
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product
                                                                                                 // sang ProductResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    public List<ProductResponse> findByPriceBetween(double minPrice, double maxPrice) { // Phương thức tìm sản phẩm theo
                                                                                        // khoảng giá
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice); // Tìm danh sách Product
                                                                                           // trong khoảng giá
        return products.stream().map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product
                                                                                                 // sang ProductResponse
                .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or hasRole('User')") // Kiểm tra quyền: chỉ Admin hoặc User được truy cập
    public List<ProductResponse> findByCriteria(String name, String category, String brand, String cpu, String ram,
            String storage, String screenSize, double price, double minPrice, double maxPrice) { // Phương thức tìm sản
                                                                                                 // phẩm theo nhiều tiêu
                                                                                                 // chí
        try {
            List<Product> products; // Khai báo biến để lưu danh sách sản phẩm
            if (name != null && brand != null) { // Kiểm tra nếu có tên và thương hiệu
                products = productRepository.findByNameAndBrand(name, brand); // Tìm sản phẩm theo tên và thương hiệu
            } else if (name != null) { // Kiểm tra nếu chỉ có tên
                products = productRepository.findByName(name); // Tìm sản phẩm theo tên
            } else if (category != null && brand != null) { // Kiểm tra nếu có danh mục và thương hiệu
                Category categoryEntity = categoryRepository.findByName(category) // Tìm Category theo tên
                        .orElseThrow(() -> new RuntimeException("Category not found with name: " + category)); // Ném
                                                                                                               // ngoại
                                                                                                               // lệ nếu
                                                                                                               // không
                                                                                                               // tìm
                                                                                                               // thấy
                                                                                                               // danh
                                                                                                               // mục
                products = productRepository.findByCategoryAndBrand(categoryEntity, brand); // Tìm sản phẩm theo danh
                                                                                            // mục và thương hiệu
            } else if (category != null) { // Kiểm tra nếu chỉ có danh mục
                Category categoryEntity = categoryRepository.findByName(category) // Tìm Category theo tên
                        .orElseThrow(() -> new RuntimeException("Category not found with name: " + category)); // Ném
                                                                                                               // ngoại
                                                                                                               // lệ nếu
                                                                                                               // không
                                                                                                               // tìm
                                                                                                               // thấy
                                                                                                               // danh
                                                                                                               // mục
                products = productRepository.findByCategory(categoryEntity); // Tìm sản phẩm theo danh mục
            } else if (brand != null) { // Kiểm tra nếu chỉ có thương hiệu
                products = productRepository.findByBrand(brand); // Tìm sản phẩm theo thương hiệu
            } else if (cpu != null) { // Kiểm tra nếu chỉ có CPU
                products = productRepository.findByCpu(cpu); // Tìm sản phẩm theo CPU
            } else if (ram != null) { // Kiểm tra nếu chỉ có RAM
                products = productRepository.findByRam(ram); // Tìm sản phẩm theo RAM
            } else if (storage != null) { // Kiểm tra nếu chỉ có bộ nhớ
                products = productRepository.findByStorage(storage); // Tìm sản phẩm theo bộ nhớ
            } else if (screenSize != null) { // Kiểm tra nếu chỉ có kích thước màn hình
                products = productRepository.findByScreenSize(screenSize); // Tìm sản phẩm theo kích thước màn hình
            } else if (price != 0) { // Kiểm tra nếu chỉ có giá
                products = productRepository.findByPrice(price); // Tìm sản phẩm theo giá
            } else if (minPrice != 0 && maxPrice != 0) { // Kiểm tra nếu có khoảng giá
                products = productRepository.findByPriceBetween(minPrice, maxPrice); // Tìm sản phẩm trong khoảng giá
            } else { // Nếu không có tiêu chí nào
                products = productRepository.findAll(); // Lấy tất cả sản phẩm
            }
            return products.stream().map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng
                                                                                                     // Product sang
                                                                                                     // ProductResponse
                    .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi
            logger.error("Failed to fetch products by criteria", e); // Ghi log lỗi
            return Collections.emptyList(); // Trả về danh sách rỗng nếu có lỗi
        }
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin') or hasRole('User')") // Kiểm tra quyền: chỉ Admin hoặc User được truy cập
    public List<ProductResponse> compareProducts(List<Integer> id) { // Phương thức so sánh nhiều sản phẩm theo danh
                                                                     // sách ID
        try {
            if (id == null || id.size() < 2) { // Kiểm tra xem danh sách ID có null hoặc ít hơn 2 phần tử không
                throw new IllegalArgumentException("Cần ít nhất 2 sản phẩm để so sánh"); // Ném ngoại lệ nếu không đủ
                                                                                         // sản phẩm để so sánh
            }

            List<Product> products = productRepository.findAllById(id); // Tìm danh sách Product theo danh sách ID

            if (products.size() != id.size()) { // Kiểm tra xem số lượng sản phẩm tìm được có khớp với số lượng ID không
                throw new RuntimeException("Một hoặc nhiều sản phẩm không tồn tại"); // Ném ngoại lệ nếu có sản phẩm
                                                                                     // không tồn tại
            }

            return products.stream() // Chuyển danh sách Product thành stream
                    .map(product -> modelMapper.map(product, ProductResponse.class)) // Ánh xạ từng Product sang
                                                                                     // ProductResponse
                    .collect(Collectors.toList()); // Thu thập kết quả thành danh sách và trả về
        } catch (Exception e) { // Bắt ngoại lệ nếu có lỗi
            logger.error("Lỗi khi so sánh sản phẩm: ", e); // Ghi log lỗi
            throw e; // Ném lại ngoại lệ để xử lý ở tầng trên
        }
    }

}