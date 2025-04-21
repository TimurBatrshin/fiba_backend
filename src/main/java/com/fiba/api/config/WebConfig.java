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
        // Включаем поддержку суффикса для сопоставления путей
        configurer.setUseTrailingSlashMatch(true);
        // Установка режима соответствия для путей с точкой в URL
        configurer.setUseSuffixPatternMatch(false);
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")  // Разрешаем запросы со всех доменов
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization")
            .maxAge(3600);
    }
} 