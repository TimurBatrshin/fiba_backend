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
        // Spring Boot 3 имеет другой API для настройки сопоставления путей
        // configurer.setUseTrailingSlashMatch устарел в новых версиях
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("https://dev.bro-js.ru", "http://localhost:8099", "https://timurbatrshin-fiba-backend-fc1f.twc1.net")  // Разрешаем запросы с локального и боевого домена
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization")
            .allowCredentials(true)  // Разрешаем передачу учетных данных
            .maxAge(3600);
    }
} 