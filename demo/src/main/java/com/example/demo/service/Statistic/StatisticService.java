package com.example.demo.service.Statistic;

import com.example.demo.dto.req.Statistic.StatisticRequest;
import com.example.demo.dto.res.Statistic.StatisticResponse;

public interface StatisticService {
    StatisticResponse getStatisticByDay(StatisticRequest statisticRequest);

    StatisticResponse generateStatisticsReport();

    StatisticResponse getById(int id);
}
