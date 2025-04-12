package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class ZalopayConfig {
    @Value("${zalopay.app_id}")
    private String appId;
    @Value("${zalopay.key1}")
    private String key1;
    @Value("${zalopay.key2}")
    private String key2;
    @Value("${zalopay.endpoint}")
    private String endpoint;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
