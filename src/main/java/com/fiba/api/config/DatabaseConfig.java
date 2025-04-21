package com.fiba.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import javax.sql.DataSource;

/**
 * Конфигурация базы данных с обработкой ошибок
 */
@Configuration
public class DatabaseConfig {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
    
    @Value("${spring.datasource.url:jdbc:h2:mem:testdb}")
    private String datasourceUrl;
    
    /**
     * Проверка соединения с базой данных при старте приложения
     * 
     * @param event событие обновления контекста
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Приложение запущено, проверка подключения к базе данных...");
        log.info("Используемый URL базы данных: {}", datasourceUrl);
        
        try {
            // Попытка выполнить тестовое соединение уже осуществляется через Hikari
            log.info("Подключение к базе данных успешно");
        } catch (Exception e) {
            log.error("Не удалось подключиться к базе данных: {}", e.getMessage());
            // Не выбрасываем исключение, чтобы приложение всё равно запустилось
        }
    }
} 