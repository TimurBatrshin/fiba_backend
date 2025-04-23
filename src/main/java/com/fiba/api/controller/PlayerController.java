package com.fiba.api.controller;

import com.fiba.api.model.Profile;
import com.fiba.api.model.User;
import com.fiba.api.service.ProfileService;
import com.fiba.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@CrossOrigin(origins = {
    "http://localhost:8099", 
    "https://dev.bro-js.ru", 
    "https://timurbatrshin-fiba-backend-fc1f.twc1.net",
    "https://timurbatrshin-fiba-backend-5ef6.twc1.net",
    "http://localhost:3000",
    "http://localhost"
}, allowCredentials = "true")
@Slf4j
public class PlayerController {

    private final ProfileService profileService;
    private final UserService userService;

    /**
     * Получение данных игрока по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlayerById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            Profile profile = profileService.getProfileByUserId(id);
            
            Map<String, Object> playerData = new HashMap<>();
            playerData.put("id", user.getId());
            playerData.put("name", user.getName());
            playerData.put("email", user.getEmail());
            playerData.put("points", profile.getTotalPoints());
            playerData.put("rating", profile.getRating());
            playerData.put("tournaments_played", profile.getTournamentsPlayed());
            playerData.put("photo_url", profile.getPhotoUrl());
            
            return ResponseEntity.ok(playerData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Игрок не найден"));
        }
    }
    
    /**
     * Получение базовой статистики игрока
     */
    @GetMapping("/{id}/stats/basic")
    public ResponseEntity<?> getPlayerBasicStats(@PathVariable Long id) {
        try {
            Profile profile = profileService.getProfileByUserId(id);
            User user = userService.getUserById(id);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("id", user.getId());
            stats.put("name", user.getName());
            stats.put("points", profile.getTotalPoints());
            stats.put("rating", profile.getRating());
            stats.put("tournaments_played", profile.getTournamentsPlayed());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Статистика игрока не найдена"));
        }
    }
    
    /**
     * Получение детальной статистики игрока
     */
    @GetMapping("/{id}/stats/detailed")
    public ResponseEntity<?> getPlayerDetailedStats(@PathVariable Long id) {
        try {
            Profile profile = profileService.getProfileByUserId(id);
            User user = userService.getUserById(id);
            
            // В реальном приложении здесь была бы более детальная статистика из БД
            Map<String, Object> stats = new HashMap<>();
            stats.put("id", user.getId());
            stats.put("name", user.getName());
            stats.put("points", profile.getTotalPoints());
            stats.put("rating", profile.getRating());
            stats.put("tournaments_played", profile.getTournamentsPlayed());
            stats.put("wins", 0); // Заглушки для демонстрации
            stats.put("losses", 0);
            stats.put("games_played", 0);
            stats.put("average_points", 0.0);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Детальная статистика игрока не найдена"));
        }
    }
    
    /**
     * Получение полной статистики игрока (соответствует требованиям фронтенда)
     * @param id идентификатор игрока
     * @return статистика игрока
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<?> getPlayerStatistics(@PathVariable Long id) {
        try {
            Profile profile = profileService.getProfileByUserId(id);
            User user = userService.getUserById(id);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("id", user.getId());
            stats.put("name", user.getName());
            stats.put("email", user.getEmail());
            stats.put("photo_url", profile.getPhotoUrl());
            
            // Базовые показатели
            stats.put("points", profile.getTotalPoints());
            stats.put("rating", profile.getRating());
            stats.put("tournaments_played", profile.getTournamentsPlayed());
            
            // Детальные показатели (могут быть расширены в будущем)
            stats.put("wins", 0);
            stats.put("losses", 0);
            stats.put("games_played", 0);
            stats.put("average_points", 0.0);
            
            // Дополнительная информация для отображения
            stats.put("position", "Guard"); // Позиция игрока (заглушка)
            stats.put("team", "Freelancer"); // Команда (заглушка)
            stats.put("rank", profile.getRating() > 80 ? "Pro" : "Amateur"); // Ранг игрока
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Статистика игрока не найдена"));
        }
    }

    /**
     * Поиск игроков по имени
     * @param params параметры поиска (query - строка для поиска)
     * @return список найденных игроков
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchPlayers(@RequestParam(required = false) Map<String, String> params) {
        String query = params.getOrDefault("query", "");
        log.info("Поиск игроков по запросу: {}", query);
        try {
            List<User> users = userService.searchUsers(query);
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (User user : users) {
                try {
                    Profile profile = profileService.getProfileByUserId(user.getId());
                    
                    Map<String, Object> playerData = new HashMap<>();
                    playerData.put("id", user.getId());
                    playerData.put("name", user.getName());
                    playerData.put("email", user.getEmail());
                    playerData.put("points", profile.getTotalPoints());
                    playerData.put("rating", profile.getRating());
                    playerData.put("tournaments_played", profile.getTournamentsPlayed());
                    playerData.put("photo_url", profile.getPhotoUrl());
                    
                    result.add(playerData);
                } catch (Exception e) {
                    // Если у пользователя нет профиля, пропускаем его
                    log.warn("Пропускаем пользователя без профиля: {}", user.getId());
                    continue;
                }
            }
            
            log.info("Найдено {} игроков", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Ошибка при поиске игроков: {}", e.getMessage(), e);
            return ResponseEntity.ok(new ArrayList<>());  // Возвращаем пустой список вместо ошибки
        }
    }

    /**
     * Получение рейтинга игроков по определенной категории
     * @param category категория рейтинга (points, rating, tournaments_played)
     * @param limit максимальное количество записей в результате
     * @return список игроков с их статистикой
     */
    @GetMapping("/rankings")
    public ResponseEntity<?> getPlayerRankings(
            @RequestParam String category,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Profile> profiles;
        
        // Фильтруем профили, чтобы исключить null значения
        List<Profile> validProfiles = profileService.getAllProfiles()
            .stream()
            .filter(profile -> profile.getTotalPoints() != null && profile.getRating() != null && profile.getTournamentsPlayed() != null)
            .collect(Collectors.toList());
        
        // Получаем список профилей и сортируем в зависимости от категории
        switch (category) {
            case "points":
                profiles = validProfiles.stream()
                    .sorted(Comparator.comparing(Profile::getTotalPoints, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
                break;
            case "rating":
                profiles = validProfiles.stream()
                    .sorted(Comparator.comparing(Profile::getRating, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
                break;
            case "tournaments":
                profiles = validProfiles.stream()
                    .sorted(Comparator.comparing(Profile::getTournamentsPlayed, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
                break;
            default:
                return ResponseEntity.badRequest().body(Map.of("error", "Неизвестная категория рейтинга"));
        }
        
        // Преобразуем в формат данных для клиента
        List<Map<String, Object>> result = new ArrayList<>();
        for (Profile profile : profiles) {
            User user = userService.getUserById(profile.getUser().getId());
            
            Map<String, Object> playerData = new HashMap<>();
            playerData.put("id", user.getId());
            playerData.put("name", user.getName());
            playerData.put("points", profile.getTotalPoints() != null ? profile.getTotalPoints() : 0);
            playerData.put("rating", profile.getRating() != null ? profile.getRating() : 0);
            playerData.put("tournaments_played", profile.getTournamentsPlayed() != null ? profile.getTournamentsPlayed() : 0);
            playerData.put("photo_url", profile.getPhotoUrl());
            
            result.add(playerData);
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Получение топ игроков для главной страницы
     * @param limit максимальное количество игроков (по умолчанию 5)
     * @return список лучших игроков
     */
    @GetMapping("/top")
    public ResponseEntity<?> getTopPlayers(@RequestParam(defaultValue = "5") int limit) {
        log.info("Запрос на получение топ-{} игроков", limit);
        
        try {
            // Фильтруем профили, чтобы исключить null значения
            List<Profile> validProfiles = profileService.getAllProfiles()
                .stream()
                .filter(profile -> profile.getTotalPoints() != null && profile.getRating() != null)
                .sorted(Comparator.comparing(Profile::getRating, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(limit)
                .collect(Collectors.toList());
            
            // Преобразуем в формат данных для клиента
            List<Map<String, Object>> result = new ArrayList<>();
            for (Profile profile : validProfiles) {
                User user = userService.getUserById(profile.getUser().getId());
                
                Map<String, Object> playerData = new HashMap<>();
                playerData.put("id", user.getId());
                playerData.put("name", user.getName());
                playerData.put("rating", profile.getRating() != null ? profile.getRating() : 0);
                playerData.put("points", profile.getTotalPoints() != null ? profile.getTotalPoints() : 0);
                playerData.put("tournaments_played", profile.getTournamentsPlayed() != null ? profile.getTournamentsPlayed() : 0);
                playerData.put("photo_url", profile.getPhotoUrl());
                playerData.put("rank", profile.getRating() > 80 ? "Pro" : "Amateur");
                
                result.add(playerData);
            }
            
            log.info("Возвращается {} лучших игроков", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Ошибка при получении топ игроков: {}", e.getMessage(), e);
            return ResponseEntity.ok(new ArrayList<>());  // Возвращаем пустой список вместо ошибки
        }
    }
} 