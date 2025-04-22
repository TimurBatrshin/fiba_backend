package com.fiba.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Конфигурация базы данных с обработкой ошибок
 */
@Configuration
public class DatabaseConfig {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
    
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    /**
     * Проверка соединения с базой данных при старте приложения
     * 
     * @param event событие обновления контекста
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Приложение запущено, проверка подключения к базе данных...");
        log.info("Используемый URL базы данных: {}", datasourceUrl);
        
        if (dataSource == null) {
            log.warn("DataSource не найден. Проверка соединения с базой данных пропущена. Убедитесь, что spring.autoconfigure.exclude не отключает DataSourceAutoConfiguration.");
            return;
        }
        
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(5);
            if (valid) {
                log.info("Подключение к базе данных успешно");
                // Выводим версию БД для диагностики
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                try {
                    String dbVersion = jdbcTemplate.queryForObject("SELECT version()", String.class);
                    log.info("Версия БД: {}", dbVersion);
                } catch (Exception e) {
                    log.warn("Не удалось получить версию БД: {}", e.getMessage());
                    
                    // Попробуем выполнить простой запрос
                    try {
                        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                        log.info("Тестовый запрос выполнен успешно: {}", result);
                    } catch (Exception e2) {
                        log.warn("Не удалось выполнить простой запрос: {}", e2.getMessage());
                    }
                }
            } else {
                log.error("Подключение к базе данных неуспешно");
            }
        } catch (SQLException e) {
            log.error("Ошибка при подключении к базе данных: {}", e.getMessage());
            log.debug("Детали ошибки:", e);
            
            // Проверим доступность базы данных
            try {
                // Попытка создать временное подключение для проверки
                log.info("Попытка создания нового соединения для диагностики...");
                SingleConnectionDataSource testDs = new SingleConnectionDataSource(
                        datasourceUrl, username, "**********", true);
                testDs.setAutoCommit(true);
                
                try (Connection conn = testDs.getConnection()) {
                    boolean isValid = conn.isValid(5);
                    log.info("Тестовое соединение: {}", isValid ? "УСПЕШНО" : "НЕУСПЕШНО");
                }
            } catch (Exception ex) {
                log.error("Дополнительная попытка подключения также не удалась: {}", ex.getMessage());
                log.error("Проверьте настройки подключения к БД в application.properties и доступность сервера БД.");
            }
        }
    }
} 