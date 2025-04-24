package com.fiba.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/**
 * Конфигурация для доступа к загруженным файлам через URL
 */
@Configuration
@Slf4j
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // Определяем абсолютный путь к директории загрузок
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            String resourceLocation = "file:" + uploadPath.toString() + "/";
            
            log.info("Configuring static resource handler:");
            log.info("Upload directory path: {}", uploadPath);
            log.info("Resource location: {}", resourceLocation);
            
            // Регистрируем обработчик ресурсов
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(resourceLocation)
                    .setCachePeriod(3600) // Кэширование на 1 час
                    .resourceChain(true);
            
            log.info("Static resource handler configured successfully");
        } catch (Exception e) {
            log.error("Error configuring static resource handler", e);
            throw e;
        }
    }
} 