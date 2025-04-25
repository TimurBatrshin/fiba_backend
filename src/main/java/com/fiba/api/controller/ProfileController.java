package com.fiba.api.controller;

import com.fiba.api.model.Profile;
import com.fiba.api.model.User;
import com.fiba.api.service.FileStorageService;
import com.fiba.api.service.ProfileService;
import com.fiba.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "http://localhost:8099", 
    "https://dev.bro-js.ru", 
    "https://timurbatrshin-fiba-backend-fc1f.twc1.net",
    "https://timurbatrshin-fiba-backend-5ef6.twc1.net",
    "http://localhost:3000",
    "http://localhost"
}, allowCredentials = "true")
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        Profile profile = profileService.getProfileByUserId(user.getId());
        
        // Подготовка данных профиля в формате JSON
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", profile.getId());
        profileData.put("user_id", user.getId());
        profileData.put("name", user.getName());
        profileData.put("email", user.getEmail());
        profileData.put("photo_url", profile.getPhotoUrl());
        profileData.put("tournaments_played", profile.getTournamentsPlayed());
        profileData.put("total_points", profile.getTotalPoints());
        profileData.put("rating", profile.getRating());
        
        return ResponseEntity.ok(profileData);
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable Long id) {
        try {
            Profile profile = profileService.getProfileById(id);
            if (profile == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Ошибка при получении профиля по ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении профиля: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/profile/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUserProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "name", required = false) String name) {
        
        try {
            log.info("Получен запрос на обновление профиля для пользователя: {}", userDetails.getUsername());
            
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Пользователь не аутентифицирован"));
            }
            
            User user = userService.getUserByEmail(userDetails.getUsername());
            log.info("Пользователь найден: {}, id: {}", user.getName(), user.getId());
            
            Profile currentProfile = profileService.getProfileByUserId(user.getId());
            log.info("Профиль найден: {}", currentProfile.getId());
            
            // Обновляем фото если оно предоставлено
            if (photo != null && !photo.isEmpty()) {
                String photoUrl = fileStorageService.storeProfilePhoto(photo);
                log.info("Фото сохранено по пути: {}", photoUrl);
                
                // Если есть предыдущее фото, удаляем его
                String oldPhotoUrl = currentProfile.getPhotoUrl();
                if (oldPhotoUrl != null && !oldPhotoUrl.isEmpty()) {
                    try {
                        String relativePath = oldPhotoUrl.substring("/uploads/".length());
                        fileStorageService.storeFile(null, relativePath);
                        log.info("Старое фото удалено: {}", oldPhotoUrl);
                    } catch (Exception e) {
                        log.warn("Не удалось удалить старое фото: {}", oldPhotoUrl, e);
                    }
                }
                
                currentProfile.setPhotoUrl(photoUrl);
            }
            
            // Обновляем email если он предоставлен
            if (email != null && !email.trim().isEmpty()) {
                // Проверяем, не занят ли email другим пользователем
                if (!email.equals(user.getEmail()) && userService.existsByEmail(email)) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Этот email уже используется"));
                }
                user.setEmail(email.trim().toLowerCase());
            }
            
            // Обновляем имя если оно предоставлено
            if (name != null && !name.trim().isEmpty()) {
                user.setName(name.trim());
            }
            
            // Сохраняем обновления
            User updatedUser = userService.updateUser(user);
            Profile updatedProfile = profileService.saveProfile(currentProfile);
            
            // Подготовка ответа
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedProfile.getId());
            response.put("user_id", updatedUser.getId());
            response.put("name", updatedUser.getName());
            response.put("email", updatedUser.getEmail());
            response.put("photo_url", updatedProfile.getPhotoUrl());
            response.put("tournaments_played", updatedProfile.getTournamentsPlayed());
            response.put("total_points", updatedProfile.getTotalPoints());
            response.put("rating", updatedProfile.getRating());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Ошибка валидации данных: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            log.error("Ошибка при сохранении фотографии: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при сохранении фотографии: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Внутренняя ошибка сервера: " + e.getMessage()));
        }
    }
} 