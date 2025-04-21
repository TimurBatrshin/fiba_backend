package com.fiba.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Общая конфигурация приложения
 */
@Configuration
public class AppConfig {

    /**
     * Создает и настраивает RestTemplate для HTTP запросов
     * 
     * @return настроенный экземпляр RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 