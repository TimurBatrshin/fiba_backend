package com.fiba.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для проверки статуса API и CORS
 */
@RestController
@RequestMapping("/api")
public class StatusController {

    /**
     * Проверка статуса API
     * 
     * @return Информация о статусе API
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "ok");
        status.put("message", "API работает нормально");
        status.put("timestamp", System.currentTimeMillis());
        status.put("cors", "Настроен для домена dev.bro-js.ru");
        
        return ResponseEntity.ok(status);
    }
} 