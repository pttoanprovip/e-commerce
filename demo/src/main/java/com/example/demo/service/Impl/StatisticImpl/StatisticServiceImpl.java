package com.example.demo.service.Impl.StatisticImpl; // Định nghĩa package chứa class này

import java.time.LocalDateTime; // Import lớp LocalDateTime để xử lý thời gian

import org.modelmapper.ModelMapper; // Import ModelMapper để ánh xạ giữa DTO và Entity
import org.springframework.scheduling.annotation.Scheduled; // Import annotation Scheduled để lập lịch chạy phương thức
import org.springframework.security.access.prepost.PreAuthorize; // Import annotation PreAuthorize để kiểm tra quyền trước khi thực thi phương thức
import org.springframework.stereotype.Service; // Import annotation Service để đánh dấu lớp này là một service
import org.springframework.transaction.annotation.Transactional; // Import annotation Transactional để quản lý giao dịch

import com.example.demo.dto.req.Statistic.StatisticRequest; // Import DTO StatisticRequest để nhận dữ liệu đầu vào
import com.example.demo.dto.res.Statistic.StatisticResponse; // Import DTO StatisticResponse để trả về dữ liệu đầu ra
import com.example.demo.entity.Statistic.Statistic; // Import lớp Statistic từ entity
import com.example.demo.repository.Statistic.StatisticRepository; // Import StatisticRepository để truy vấn Statistic
import com.example.demo.service.Statistic.StatisticService; // Import interface StatisticService mà lớp này triển khai

@Service // Đánh dấu lớp này là một Spring Service
public class StatisticServiceImpl implements StatisticService { // Lớp triển khai interface StatisticService

    private StatisticRepository statisticRepository; // Khai báo biến StatisticRepository để tương tác với cơ sở dữ liệu
                                                     // Statistic
    private final ModelMapper modelMapper; // Khai báo biến ModelMapper để ánh xạ đối tượng

    // Constructor để tiêm các dependency
    public StatisticServiceImpl(StatisticRepository statisticRepository, ModelMapper modelMapper) {
        this.statisticRepository = statisticRepository; // Gán StatisticRepository được tiêm vào biến instance
        this.modelMapper = modelMapper; // Gán ModelMapper được tiêm vào biến instance
    }

    @Override // Ghi đè phương thức từ interface
    @Transactional // Đánh dấu phương thức này là một giao dịch, tự động quản lý rollback nếu có
                   // lỗi
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được phép truy cập phương thức này
    public StatisticResponse getStatisticByDay(StatisticRequest statisticRequest) { // Phương thức lấy thống kê theo
                                                                                    // khoảng thời gian

        Statistic statistic = new Statistic(); // Tạo một đối tượng Statistic mới để lưu trữ dữ liệu thống kê
        LocalDateTime start = statisticRequest.getStartDate(); // Lấy thời gian bắt đầu từ request
        LocalDateTime end = statisticRequest.getEndDate(); // Lấy thời gian kết thúc từ request

        int totalOrders = statisticRepository.countOrderByDay(start, end); // Đếm tổng số đơn hàng trong khoảng thời
                                                                           // gian
        Double totalRevenue = statisticRepository.sumRevenueByDay(start, end); // Tính tổng doanh thu trong khoảng thời
                                                                               // gian
        Integer totalProductSold = statisticRepository.sumProductsSoldByDay(start, end); // Tính tổng số sản phẩm đã bán
                                                                                         // trong khoảng thời gian

        statistic.setStartDate(start); // Gán thời gian bắt đầu cho đối tượng Statistic
        statistic.setEndDate(end); // Gán thời gian kết thúc cho đối tượng Statistic
        statistic.setTotalOrder(totalOrders); // Gán tổng số đơn hàng cho đối tượng Statistic
        statistic.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0); // Gán tổng doanh thu, nếu null thì gán
                                                                              // 0.0
        statistic.setTotalProductSold(totalProductSold != null ? totalProductSold : 0); // Gán tổng số sản phẩm đã bán,
                                                                                        // nếu null thì gán 0

        return modelMapper.map(statistic, StatisticResponse.class); // Ánh xạ đối tượng Statistic sang StatisticResponse
                                                                    // và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @Scheduled(cron = "0 0 0 * * *") // Lập lịch chạy phương thức này vào 00:00:00 mỗi ngày
    @Transactional // Đánh dấu phương thức này là một giao dịch
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được phép chạy phương thức này
    public StatisticResponse generateStatisticsReport() { // Phương thức tạo báo cáo thống kê hàng ngày

        Statistic statistic = new Statistic(); // Tạo một đối tượng Statistic mới để lưu trữ dữ liệu thống kê
        LocalDateTime now = LocalDateTime.now(); // Lấy thời gian hiện tại
        LocalDateTime start = now.toLocalDate().atStartOfDay(); // Lấy thời gian bắt đầu của ngày hiện tại (00:00:00)
        LocalDateTime end = now.toLocalDate().atTime(23, 59, 59); // Lấy thời gian kết thúc của ngày hiện tại (23:59:59)

        statistic.setCreateAt(now); // Gán thời gian tạo báo cáo là thời gian hiện tại
        statistic.setStartDate(start); // Gán thời gian bắt đầu cho đối tượng Statistic
        statistic.setEndDate(end); // Gán thời gian kết thúc cho đối tượng Statistic

        int totalOrders = statisticRepository.countOrderByDay(start, end); // Đếm tổng số đơn hàng trong ngày
        Double totalRevenue = statisticRepository.sumRevenueByDay(start, end); // Tính tổng doanh thu trong ngày
        Integer totalProductSold = statisticRepository.sumProductsSoldByDay(start, end); // Tính tổng số sản phẩm đã bán
                                                                                         // trong ngày

        statistic.setTotalOrder(totalOrders); // Gán tổng số đơn hàng cho đối tượng Statistic
        statistic.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0); // Gán tổng doanh thu, nếu null thì gán
                                                                              // 0.0
        statistic.setTotalProductSold(totalProductSold != null ? totalProductSold : 0); // Gán tổng số sản phẩm đã bán,
                                                                                        // nếu null thì gán 0

        Statistic saveStatistic = statisticRepository.save(statistic); // Lưu đối tượng Statistic vào cơ sở dữ liệu

        return modelMapper.map(saveStatistic, StatisticResponse.class); // Ánh xạ đối tượng Statistic đã lưu sang
                                                                        // StatisticResponse và trả về
    }

    @Override // Ghi đè phương thức từ interface
    @PreAuthorize("hasRole('Admin')") // Kiểm tra quyền: chỉ Admin được phép truy cập phương thức này
    public StatisticResponse getById(int id) { // Phương thức lấy thông tin thống kê theo ID

        Statistic statistic = statisticRepository.findById(id) // Tìm Statistic theo ID
                .orElseThrow(() -> new RuntimeException("Statistic not found")); // Ném ngoại lệ nếu không tìm thấy
                                                                                 // thống kê

        return modelMapper.map(statistic, StatisticResponse.class); // Ánh xạ đối tượng Statistic sang StatisticResponse
                                                                    // và trả về
    }
}