package com.example.oxo.config;

import com.example.oxo.monitoring.PerformanceStats;
import com.example.oxo.service.CachedGameService;
import com.example.oxo.service.GameService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ServiceConfig {

    @Bean
    @Primary
    public GameService gameService() {
        return new CachedGameService(); // 使用优化版本的服务
    }
    
    @Bean
    public PerformanceStats performanceStats() {
        return new PerformanceStats();
    }
}