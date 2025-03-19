package com.example.demo.service.Impl.StatisticImpl;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.req.Statistic.StatisticRequest;
import com.example.demo.dto.res.Statistic.StatisticResponse;
import com.example.demo.entity.Statistic.Statistic;
import com.example.demo.repository.Statistic.StatisticRepository;
import com.example.demo.service.Statistic.StatisticService;

@Service
public class StatisticServiceImpl implements StatisticService {

    private StatisticRepository statisticRepository;
    private final ModelMapper modelMapper;

    public StatisticServiceImpl(StatisticRepository statisticRepository, ModelMapper modelMapper) {
        this.statisticRepository = statisticRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('Admin')")
    public StatisticResponse getStatisticByDay(StatisticRequest statisticRequest) {
        Statistic statistic = new Statistic();
        LocalDateTime start = statisticRequest.getStartDate();
        LocalDateTime end = statisticRequest.getEndDate();

        int totalOrders = statisticRepository.countOrderByDay(start, end);
        Double totalRevenue = statisticRepository.sumRevenueByDay(start, end);
        Integer totalProductSold = statisticRepository.sumProductsSoldByDay(start, end);

        statistic.setStartDate(start);
        statistic.setEndDate(end);
        statistic.setTotalOrder(totalOrders);
        statistic.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);
        statistic.setTotalProductSold(totalProductSold != null ? totalProductSold : 0);

        return modelMapper.map(statistic, StatisticResponse.class);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    @PreAuthorize("hasRole('Admin')")
    public StatisticResponse generateStatisticsReport() {
        Statistic statistic = new Statistic();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.toLocalDate().atStartOfDay();
        LocalDateTime end = now.toLocalDate().atTime(23, 59, 59);

        statistic.setCreateAt(now);
        statistic.setStartDate(start);
        statistic.setEndDate(end);

        int totalOrders = statisticRepository.countOrderByDay(start, end);
        Double totalRevenue = statisticRepository.sumRevenueByDay(start, end);
        Integer totalProductSold = statisticRepository.sumProductsSoldByDay(start, end);

        statistic.setTotalOrder(totalOrders);
        statistic.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);
        statistic.setTotalProductSold(totalProductSold != null ? totalProductSold : 0);

        Statistic saveStatistic = statisticRepository.save(statistic);

        return modelMapper.map(saveStatistic, StatisticResponse.class);
    }

    @Override
    @PreAuthorize("hasRole('Admin')")
    public StatisticResponse getById(int id) {
        Statistic statistic = statisticRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Statistic not found"));

        return modelMapper.map(statistic, StatisticResponse.class);
    }
}