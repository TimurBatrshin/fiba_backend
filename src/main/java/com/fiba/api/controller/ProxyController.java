package com.fiba.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Enumeration;

/**
 * Контроллер для проксирования запросов к внешним ресурсам
 */
@RestController
@RequestMapping("/api/proxy")
public class ProxyController {

    @Autowired
    private RestTemplate restTemplate;
    
    // Параметры для аутентификации - можно установить через переменные окружения
    @Value("${proxy.static-bro-js.auth.enabled:false}")
    private boolean authEnabled;
    
    @Value("${proxy.static-bro-js.auth.username:}")
    private String username;
    
    @Value("${proxy.static-bro-js.auth.password:}")
    private String password;

    /**
     * Проксирует запросы к static.bro-js.ru
     * 
     * @return содержимое ресурса с внешнего сервера
     */
    @GetMapping("/static-bro-js/**")
    public ResponseEntity<String> proxyStaticBroJs() throws URISyntaxException {
        // Получаем текущий запрос
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        
        // Получаем URI запроса
        String requestURI = request.getRequestURI();
        
        // URI будет вида /api/proxy/static-bro-js/path/to/resource
        // Нам нужно извлечь часть после /api/proxy/static-bro-js/
        String fullPath = requestURI.substring("/api/proxy/static-bro-js/".length());
        
        // Формируем URL для запроса к внешнему серверу
        String url = "https://static.bro-js.ru/" + fullPath;
        
        // Проверяем, является ли файл JavaScript-файлом
        boolean isJsFile = fullPath.endsWith(".js");
        
        // Создаем заголовки для запроса
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "*/*");
        headers.add("User-Agent", "Spring RestTemplate");
        
        // Если включена аутентификация, добавляем заголовок Basic Authentication
        if (authEnabled && username != null && !username.isEmpty()) {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            String authHeader = "Basic " + new String(encodedAuth);
            headers.add("Authorization", authHeader);
        }
        
        // Создаем HTTP-сущность с заголовками
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            // Выполняем запрос к внешнему серверу и возвращаем его ответ
            ResponseEntity<String> response = restTemplate.exchange(
                new URI(url),
                HttpMethod.GET,
                entity,
                String.class
            );
            
            // Создаем новые заголовки ответа
            HttpHeaders responseHeaders = new HttpHeaders();
            
            // Копируем важные заголовки из ответа
            if (response.getHeaders().getContentType() != null) {
                responseHeaders.setContentType(response.getHeaders().getContentType());
            } else if (isJsFile) {
                // Если файл JS, устанавливаем правильный Content-Type
                responseHeaders.setContentType(MediaType.parseMediaType("application/javascript"));
            }
            
            // Разрешаем доступ всем источникам
            responseHeaders.add("Access-Control-Allow-Origin", "*");
            responseHeaders.add("Access-Control-Allow-Methods", "GET, OPTIONS");
            responseHeaders.add("Access-Control-Allow-Headers", "Origin, Content-Type, Accept");
            
            // Возвращаем ответ с новыми заголовками
            return new ResponseEntity<>(
                response.getBody(),
                responseHeaders,
                response.getStatusCode()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body("Error proxying request to " + url + ": " + e.getMessage());
        }
    }
} 