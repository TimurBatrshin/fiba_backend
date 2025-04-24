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

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> updateData) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        Profile currentProfile = profileService.getProfileByUserId(user.getId());
        
        // Обновляем поля профиля если они есть в запросе
        if (updateData.containsKey("photo_url")) {
            currentProfile.setPhotoUrl((String) updateData.get("photo_url"));
        }
        if (updateData.containsKey("tournaments_played")) {
            currentProfile.setTournamentsPlayed((Integer) updateData.get("tournaments_played"));
        }
        if (updateData.containsKey("total_points")) {
            currentProfile.setTotalPoints((Integer) updateData.get("total_points"));
        }
        if (updateData.containsKey("rating")) {
            currentProfile.setRating((Integer) updateData.get("rating"));
        }
        
        // Сохранение обновленного профиля
        Profile updatedProfile = profileService.saveProfile(currentProfile);
        
        // Подготовка данных обновленного профиля в формате JSON
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", updatedProfile.getId());
        profileData.put("user_id", user.getId());
        profileData.put("name", user.getName());
        profileData.put("email", user.getEmail());
        profileData.put("photo_url", updatedProfile.getPhotoUrl());
        profileData.put("tournaments_played", updatedProfile.getTournamentsPlayed());
        profileData.put("total_points", updatedProfile.getTotalPoints());
        profileData.put("rating", updatedProfile.getRating());
        
        return ResponseEntity.ok(profileData);
    }

    @PostMapping("/profile/photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "tournaments_played", required = false) Integer tournamentsPlayed,
            @RequestParam(value = "total_points", required = false) Integer totalPoints,
            @RequestParam(value = "rating", required = false) Integer rating) {
        
        try {
            log.info("Получен запрос на загрузку фото профиля для пользователя: {}", userDetails.getUsername());
            
            User user = userService.getUserByEmail(userDetails.getUsername());
            log.info("Пользователь найден: {}, id: {}", user.getName(), user.getId());
            
            Profile currentProfile = profileService.getProfileByUserId(user.getId());
            log.info("Профиль найден: {}", currentProfile.getId());
            
            // Проверяем файл
            if (photo == null || photo.isEmpty()) {
                log.error("Файл не был передан или пустой");
                return ResponseEntity.badRequest().body(Map.of("error", "Файл не был передан или пустой"));
            }
            
            log.info("Загружаемый файл: имя={}, размер={}, тип={}",
                    photo.getOriginalFilename(), photo.getSize(), photo.getContentType());
            
            // Сохраняем фото и получаем URL
            String photoUrl = fileStorageService.storeProfilePhoto(photo);
            log.info("Фото сохранено по пути: {}", photoUrl);
            
            currentProfile.setPhotoUrl(photoUrl);
            
            // Обновляем другие поля профиля если они переданы
            if (tournamentsPlayed != null) {
                currentProfile.setTournamentsPlayed(tournamentsPlayed);
            }
            if (totalPoints != null) {
                currentProfile.setTotalPoints(totalPoints);
            }
            if (rating != null) {
                currentProfile.setRating(rating);
            }
            
            // Сохранение обновленного профиля
            Profile updatedProfile = profileService.saveProfile(currentProfile);
            log.info("Профиль обновлен успешно");
            
            // Подготовка данных обновленного профиля в формате JSON
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("id", updatedProfile.getId());
            profileData.put("user_id", user.getId());
            profileData.put("name", user.getName());
            profileData.put("email", user.getEmail());
            profileData.put("photo_url", updatedProfile.getPhotoUrl());
            profileData.put("tournaments_played", updatedProfile.getTournamentsPlayed());
            profileData.put("total_points", updatedProfile.getTotalPoints());
            profileData.put("rating", updatedProfile.getRating());
            
            return ResponseEntity.ok(profileData);
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

    @PostMapping(value = "/profile/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfilePhotoById(
            @PathVariable Long id,
            @RequestParam(value = "photo", required = false) MultipartFile photoParam,
            @RequestPart(value = "file", required = false) MultipartFile fileParam) {
        
        try {
            log.info("Получен запрос на загрузку фото для профиля с ID: {}", id);
            
            // Определяем, какой параметр использовать
            MultipartFile photo = photoParam != null ? photoParam : fileParam;
            
            // Проверяем файл
            if (photo == null || photo.isEmpty()) {
                log.error("Файл не был передан или пустой");
                // Проверка наличия файлов в запросе
                Map<String, MultipartFile> files = new HashMap<>();
                try {
                    HttpServletRequest request = (HttpServletRequest)
                            RequestContextHolder.currentRequestAttributes().resolveReference("request");

                    Map<String, MultipartFile> fileMap = Map.of();

                    if (request instanceof MultipartHttpServletRequest multipartRequest) {
                        // Получаем карту файлов (по одному файлу на ключ)
                        fileMap = multipartRequest.getFileMap();
                    }
                    log.info("Параметры запроса: {}", fileMap.keySet());
                } catch (Exception e) {
                    log.error("Ошибка при получении файлов из запроса: {}", e.getMessage());
                }
                
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Файл не был передан или пустой", 
                    "debug", "Проверьте, что файл отправляется с именем параметра 'photo' или 'file'"
                ));
            }
            
            // Находим профиль по id
            Profile profile = profileService.getProfileById(id);
            if (profile == null) {
                log.error("Профиль с ID {} не найден", id);
                return ResponseEntity.badRequest().body(Map.of("error", "Профиль не найден"));
            }
            
            log.info("Профиль найден. Загружаемый файл: имя={}, размер={}, тип={}",
                    photo.getOriginalFilename(), photo.getSize(), photo.getContentType());
            
            // Сохраняем фото и получаем URL
            String photoUrl = fileStorageService.storeProfilePhoto(photo);
            log.info("Фото сохранено по пути: {}", photoUrl);
            
            profile.setPhotoUrl(photoUrl);
            
            // Сохраняем обновленный профиль
            Profile updatedProfile = profileService.saveProfile(profile);
            log.info("Профиль обновлен успешно");
            
            // Возвращаем результат
            Map<String, Object> result = new HashMap<>();
            result.put("id", updatedProfile.getId());
            result.put("photo_url", updatedProfile.getPhotoUrl());
            result.put("message", "Фотография профиля успешно загружена");
            
            return ResponseEntity.ok(result);
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

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername());
        
        // Используем правильный метод
        Profile profile = profileService.getProfileByUserId(user.getId());
        
        return ResponseEntity.ok(profile);
    }
} 