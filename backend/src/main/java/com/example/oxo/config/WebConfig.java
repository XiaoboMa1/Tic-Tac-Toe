package com.example.oxo.config;  // 包名必须与路径一致

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")  // 允许所有API路径
				.allowedOrigins(
						"https://tic-tac-toe-1-fr16.onrender.com",  // 替换为实际前端域名
						"http://localhost:5173"          // 本地开发环境
				)
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				.allowCredentials(true);
	}
}