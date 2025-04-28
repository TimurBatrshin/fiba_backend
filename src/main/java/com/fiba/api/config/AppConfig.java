package com.fiba.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * Общая конфигурация приложения
 */
@Slf4j
@Configuration
public class AppConfig {

    /**
     * Создает и настраивает RestTemplate для HTTP запросов
     * 
     * @return настроенный экземпляр RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Добавляем логирование запросов
        restTemplate.getInterceptors().add((request, body, execution) -> {
            log.info("Making request to: {} {}", request.getMethod(), request.getURI());
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }
} 