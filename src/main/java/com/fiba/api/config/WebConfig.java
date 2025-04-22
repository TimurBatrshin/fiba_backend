package com.fiba.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация веб-приложения, включая CORS и сопоставление путей
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Используем обновленный API для конфигурации соответствия путей
        configurer.setUseTrailingSlashMatch(true);
        // Удаляем устаревший метод, так как в Spring Boot 3.x этот функционал изменен
        // configurer.setUseSuffixPatternMatch(false);
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("https://dev.bro-js.ru")  // Разрешаем запросы только с конкретного домена
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization")
            .allowCredentials(true)  // Разрешаем передачу учетных данных
            .maxAge(3600);
    }
} 