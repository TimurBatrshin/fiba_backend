package com.fiba.api.config;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {
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