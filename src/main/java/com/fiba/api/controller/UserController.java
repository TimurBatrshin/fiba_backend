package com.fiba.api.controller;

import com.fiba.api.model.User;
import com.fiba.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
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
public class UserController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/api/users/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Пользователь не аутентифицирован"));
        }
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            return ResponseEntity.ok(convertToMap(user));
        } catch (Exception e) {
            log.error("Ошибка при получении данных пользователя: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при получении данных пользователя"));
        }
    }

    @PutMapping("/api/users/me")
    public ResponseEntity<?> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> userData) {
        try {
            User currentUser = userService.getUserByEmail(userDetails.getUsername());
            Map<String, String> errors = new HashMap<>();
            
            // Проверяем и обновляем имя
            if (userData.containsKey("name")) {
                String newName = userData.get("name");
                if (newName == null || newName.trim().isEmpty()) {
                    errors.put("name", "Имя не может быть пустым");
                } else if (newName.length() < MIN_NAME_LENGTH) {
                    errors.put("name", "Имя должно содержать минимум " + MIN_NAME_LENGTH + " символа");
                } else if (newName.length() > MAX_NAME_LENGTH) {
                    errors.put("name", "Имя не может быть длиннее " + MAX_NAME_LENGTH + " символов");
                } else {
                    currentUser.setName(newName.trim());
                }
            }
            
            // Проверяем и обновляем email
            if (userData.containsKey("email")) {
                String newEmail = userData.get("email");
                if (newEmail == null || newEmail.trim().isEmpty()) {
                    errors.put("email", "Email не может быть пустым");
                } else if (!EMAIL_PATTERN.matcher(newEmail).matches()) {
                    errors.put("email", "Некорректный формат email");
                } else if (!newEmail.equals(currentUser.getEmail()) && userService.existsByEmail(newEmail)) {
                    errors.put("email", "Этот email уже используется");
                } else {
                    currentUser.setEmail(newEmail.trim().toLowerCase());
                }
            }

            // Если есть ошибки, возвращаем их
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("errors", errors));
            }
            
            User updatedUser = userService.updateUser(currentUser);
            log.info("Пользователь {} успешно обновил свой профиль", updatedUser.getId());
            return ResponseEntity.ok(convertToMap(updatedUser));
        } catch (Exception e) {
            log.error("Ошибка при обновлении данных пользователя: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Ошибка при обновлении данных пользователя",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/api/user/role")
    public ResponseEntity<?> getUserRole(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            return ResponseEntity.ok(Map.of("role", user.getRole()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка при получении роли пользователя"));
        }
    }

    @GetMapping("/api/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<Map<String, Object>> userData = users.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userData);
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(convertToMap(user));
    }

    @PutMapping("/api/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id, principal)")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> userData,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        User existingUser = userService.getUserById(id);
        
        // Только администраторы могут менять роли пользователей
        if (userData.containsKey("role") && !"admin".equals(currentUser.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Только администраторы могут менять роли пользователей"));
        }
        
        if (userData.containsKey("name")) {
            existingUser.setName((String) userData.get("name"));
        }
        
        if (userData.containsKey("email")) {
            existingUser.setEmail((String) userData.get("email"));
        }
        
        if (userData.containsKey("password")) {
            existingUser.setPassword(passwordEncoder.encode((String) userData.get("password")));
        }
        
        if (userData.containsKey("role") && "admin".equals(currentUser.getRole())) {
            existingUser.setRole((String) userData.get("role"));
        }
        
        User updatedUser = userService.updateUser(existingUser);
        
        return ResponseEntity.ok(convertToMap(updatedUser));
    }

    @DeleteMapping("/api/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().body(Map.of("message", "Пользователь успешно удален"));
    }

    @PutMapping("/api/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> roleData) {
        
        String role = roleData.get("role");
        if (role == null || !List.of("user", "admin", "advertiser").contains(role)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Недопустимая роль"));
        }
        
        User user = userService.getUserById(id);
        user.setRole(role);
        User updatedUser = userService.updateUser(user);
        
        return ResponseEntity.ok(convertToMap(updatedUser));
    }

    /**
     * Метод преобразует сущность User в Map для безопасной передачи клиенту
     * (без конфиденциальных данных, таких как пароль)
     */
    private Map<String, Object> convertToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole());
        userMap.put("email_verified", user.isEmailVerified());
        
        if (user.getProfile() != null) {
            Map<String, Object> profileMap = new HashMap<>();
            profileMap.put("id", user.getProfile().getId());
            profileMap.put("photo_url", user.getProfile().getPhotoUrl());
            profileMap.put("tournaments_played", user.getProfile().getTournamentsPlayed());
            profileMap.put("total_points", user.getProfile().getTotalPoints());
            profileMap.put("rating", user.getProfile().getRating());
            
            userMap.put("profile", profileMap);
        }
        
        return userMap;
    }
} 