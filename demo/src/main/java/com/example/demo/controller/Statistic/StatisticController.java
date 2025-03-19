package com.example.demo.controller.Statistic;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.req.Statistic.StatisticRequest;
import com.example.demo.dto.res.Statistic.StatisticResponse;
import com.example.demo.service.Statistic.StatisticService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/statistics")
public class StatisticController {
    private StatisticService statisticService;

    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @PostMapping("/by-date")
    public ResponseEntity<?> getStatisticByDay(@RequestBody StatisticRequest statisticRequest) {
        try {
            StatisticResponse statistic = statisticService.getStatisticByDay(statisticRequest);
            return ResponseEntity.ok(statistic);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStatisticById(@PathVariable int id) {
        try {
            StatisticResponse statistic = statisticService.getById(id);
            return ResponseEntity.ok(statistic);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateStatistics() {
        try {
            StatisticResponse statistic = statisticService.generateStatisticsReport();
            return ResponseEntity.ok(statistic);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
