package com.fiba.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class ApiApplication {

    public static void main(String[] args) {
        // Вывод информации о переменных окружения при запуске
        System.out.println("PORT environment: " + System.getenv("PORT"));
        System.out.println("RAILWAY_ENVIRONMENT: " + System.getenv("RAILWAY_ENVIRONMENT"));
        
        SpringApplication.run(ApiApplication.class, args);
    }
} 