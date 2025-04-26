package com.fiba.api.controller;

import com.fiba.api.service.FileStorageService;
import com.fiba.api.service.UserService;
import com.fiba.api.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;
    private final UserService userService;

    @GetMapping("/{type}/{filename:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String type,
            @PathVariable String filename) {
        try {
            // Проверяем тип файла
            if (!isValidFileType(type)) {
                return ResponseEntity.badRequest().build();
            }

            // Получаем файл
            Path filePath = Paths.get("uploads", type, filename);
            Resource resource = fileStorageService.loadFileAsResource(filePath);

            if (resource.exists() && resource.isReadable()) {
                // Определяем тип контента
                String contentType = determineContentType(filename);
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            log.error("Ошибка при получении файла: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}/avatar")
    public ResponseEntity<?> getUserAvatar(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            if (user.getProfile() != null && user.getProfile().getPhotoUrl() != null) {
                Map<String, String> response = new HashMap<>();
                response.put("avatar_url", user.getProfile().getPhotoUrl());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Ошибка при получении аватарки пользователя: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/user/{userId}/avatar")
    public ResponseEntity<?> uploadUserAvatar(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // Сохраняем файл
            String avatarUrl = fileStorageService.storeAvatar(file);
            
            // Обновляем профиль пользователя
            if (user.getProfile() != null) {
                user.getProfile().setPhotoUrl(avatarUrl);
                userService.updateUser(user);
            }

            Map<String, String> response = new HashMap<>();
            response.put("avatar_url", avatarUrl);
            response.put("message", "Аватарка успешно загружена");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ошибка при загрузке аватарки: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Ошибка при загрузке аватарки: " + e.getMessage()));
        }
    }

    private boolean isValidFileType(String type) {
        return type.equals("profiles") || 
               type.equals("avatars") || 
               type.equals("tournaments") || 
               type.equals("sponsors") || 
               type.equals("ads") || 
               type.equals("teams");
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
} 