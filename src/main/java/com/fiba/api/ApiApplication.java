package com.fiba.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.core.env.Environment;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableJpaRepositories
public class ApiApplication {

    public static void main(String[] args) {
        // Вывод информации о переменных окружения при запуске
        System.out.println("PORT environment: " + System.getenv("PORT"));
        System.out.println("RAILWAY_ENVIRONMENT: " + System.getenv("RAILWAY_ENVIRONMENT"));
        
        SpringApplication.run(ApiApplication.class, args);
    }
    
    @Component
    @Slf4j
    public static class StartupListener implements ApplicationListener<ContextRefreshedEvent> {
        private final Environment environment;
        
        public StartupListener(Environment environment) {
            this.environment = environment;
        }
        
        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            String port = environment.getProperty("server.port");
            String address = environment.getProperty("server.address");
            
            log.info("=======================================================");
            log.info("Приложение запущено на порту: {}", port);
            log.info("Адрес сервера: {}", address);
            log.info("=======================================================");
            
            System.out.println("=======================================================");
            System.out.println("Приложение запущено на порту: " + port);
            System.out.println("Адрес сервера: " + address);
            System.out.println("=======================================================");
        }
    }
} 