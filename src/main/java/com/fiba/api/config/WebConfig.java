package com.fiba.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Конфигурация веб-приложения, включая CORS и сопоставление путей
 */
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Spring Boot 3 имеет другой API для настройки сопоставления путей
        // configurer.setUseTrailingSlashMatch устарел в новых версиях
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            String resourceLocation = "file:" + uploadPath.toString() + "/";
            
            log.info("Configuring static resource handler:");
            log.info("Upload directory path: {}", uploadPath);
            log.info("Resource location: {}", resourceLocation);
            
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(resourceLocation);
                    
            log.info("Static resource handlers configured successfully");
        } catch (Exception e) {
            log.error("Error configuring static resource handlers", e);
            throw e;
        }
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:8099",
                    "http://localhost:3000",
                    "https://dev.bro-js.ru",
                    "https://timurbatrshin-fiba-backend-fc1f.twc1.net",
                    "https://timurbatrshin-fiba-backend-5ef6.twc1.net"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
} 