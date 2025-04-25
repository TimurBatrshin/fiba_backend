package com.fiba.api.controller;

import com.fiba.api.dto.TeamRegistrationRequest;
import com.fiba.api.model.Registration;
import com.fiba.api.model.Team;
import com.fiba.api.model.Tournament;
import com.fiba.api.model.TournamentStatus;
import com.fiba.api.model.TournamentTeam;
import com.fiba.api.model.User;
import com.fiba.api.model.TeamStatus;
import com.fiba.api.model.Player;
import com.fiba.api.service.RegistrationService;
import com.fiba.api.service.TournamentService;
import com.fiba.api.service.UserService;
import com.fiba.api.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер для работы с турнирами
 */
@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private static final Logger log = LoggerFactory.getLogger(TournamentController.class);
    
    private final TournamentService tournamentService;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final FileStorageService fileStorageService;

    /**
     * Конструктор для внедрения зависимостей
     * 
     * @param tournamentService сервис турниров
     * @param userService сервис пользователей
     * @param registrationService сервис регистраций
     * @param fileStorageService сервис хранения файлов
     */
    @Autowired
    public TournamentController(
            TournamentService tournamentService,
            UserService userService,
            RegistrationService registrationService,
            FileStorageService fileStorageService) {
        this.tournamentService = tournamentService;
        this.userService = userService;
        this.registrationService = registrationService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Получение списка всех турниров с возможностью фильтрации и сортировки
     */
    @GetMapping
    public ResponseEntity<?> getAllTournaments(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) Boolean upcoming) {
        
        try {
            log.info("Получен запрос на получение турниров: limit={}, sort={}, direction={}, upcoming={}", 
                    limit, sort, direction, upcoming);
            
            List<Tournament> tournaments = tournamentService.getAllTournaments(sort, direction);
            
            // Фильтрация по предстоящим/прошедшим
            if (upcoming != null) {
                LocalDate today = LocalDate.now();
                tournaments = tournaments.stream()
                    .filter(t -> upcoming ? 
                            t.getDate().isEqual(today) || t.getDate().isAfter(today) :
                            t.getDate().isBefore(today))
                    .collect(Collectors.toList());
            }
            
            // Ограничение количества
            if (limit != null && limit > 0 && limit < tournaments.size()) {
                tournaments = tournaments.subList(0, limit);
            }
            
            List<Map<String, Object>> result = tournaments.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
            
            log.info("Возвращается {} турниров", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Ошибка при получении турниров", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении списка турниров: " + e.getMessage()));
        }
    }

    /**
     * Получение турнира по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTournamentById(@PathVariable Long id) {
        try {
            Tournament tournament = tournamentService.getTournamentById(id);
            return ResponseEntity.ok(convertToMap(tournament));
        } catch (Exception e) {
            log.error("Ошибка при получении турнира с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Турнир не найден: " + e.getMessage()));
        }
    }

    /**
     * Получение турниров по статусу
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTournamentsByStatus(@PathVariable String status) {
        try {
            List<Tournament> tournaments = tournamentService.getTournamentsByStatus(status);
            List<Map<String, Object>> tournamentData = tournaments.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(tournamentData);
        } catch (Exception e) {
            log.error("Ошибка при получении турниров со статусом: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении турниров: " + e.getMessage()));
        }
    }

    /**
     * Получение предстоящих турниров
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingTournaments() {
        try {
            log.info("Получен запрос на получение предстоящих турниров");
            
            List<Tournament> upcomingTournaments = tournamentService.getUpcomingTournaments();
            
            List<Map<String, Object>> result = upcomingTournaments.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
            
            log.info("Возвращается {} предстоящих турниров", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Ошибка при получении предстоящих турниров", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении предстоящих турниров: " + e.getMessage()));
        }
    }

    /**
     * Получение прошедших турниров
     */
    @GetMapping("/past")
    public ResponseEntity<?> getPastTournaments() {
        try {
            log.info("Получен запрос на получение прошедших турниров");
            
            List<Tournament> pastTournaments = tournamentService.getCompletedTournaments();
            
            List<Map<String, Object>> result = pastTournaments.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
            
            log.info("Возвращается {} прошедших турниров", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Ошибка при получении прошедших турниров", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении прошедших турниров: " + e.getMessage()));
        }
    }

    /**
     * Поиск турниров по запросу
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchTournaments(@RequestParam String query) {
        try {
            List<Tournament> tournaments = tournamentService.searchTournaments(query);
            List<Map<String, Object>> tournamentData = tournaments.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(tournamentData);
        } catch (Exception e) {
            log.error("Ошибка при поиске турниров по запросу: {}", query, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при поиске турниров: " + e.getMessage()));
        }
    }

    /**
     * Получение турниров по уровню
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<?> getTournamentsByLevel(@PathVariable String level) {
        try {
            List<Tournament> tournaments = tournamentService.getTournamentsByLevel(level);
            List<Map<String, Object>> tournamentData = tournaments.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(tournamentData);
        } catch (Exception e) {
            log.error("Ошибка при получении турниров по уровню: {}", level, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении турниров: " + e.getMessage()));
        }
    }

    /**
     * Создание нового турнира (только для администраторов)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTournament(
            @RequestParam(value = "title") String title,
            @RequestParam(value = "date") String dateStr,
            @RequestParam(value = "location") String location,
            @RequestParam(value = "level") String level,
            @RequestParam(value = "prize_pool") Integer prizePool,
            @RequestParam(value = "tournament_image", required = false) MultipartFile tournamentImage) {
        
        try {
            log.info("Получен запрос на создание турнира: {}, {}, {}, {}", title, dateStr, location, level);
            
            LocalDate date;
            try {
                // Пробуем разные форматы дат
                if (dateStr.contains("T")) {
                    // Формат ISO с временем
                    date = LocalDateTime.parse(dateStr).toLocalDate();
                } else {
                    // Стандартный формат LocalDate
                    date = LocalDate.parse(dateStr);
                }
            } catch (Exception e) {
                log.error("Ошибка при парсинге даты: {}", dateStr, e);
                return ResponseEntity.badRequest().body(Map.of("error", "Неверный формат даты. Используйте формат YYYY-MM-DD"));
            }
            
            // Сохраняем изображение турнира, если оно было загружено
            String imageUrl = null;
            if (tournamentImage != null && !tournamentImage.isEmpty()) {
                imageUrl = fileStorageService.storeTournamentImage(tournamentImage);
                log.info("Сохранено изображение турнира: {}", imageUrl);
            }
            
            // Создаем новый турнир
            Tournament tournament = Tournament.builder()
                    .name(title)
                    .date(date)
                    .location(location)
                    .level(level)
                    .prizePool(String.valueOf(prizePool))
                    .status(TournamentStatus.UPCOMING)
                    .imageUrl(imageUrl)
                    .registrationOpen(true) // По умолчанию регистрация открыта
                    .build();
            
            Tournament createdTournament = tournamentService.createTournament(tournament);
            log.info("Турнир успешно создан с ID: {}", createdTournament.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToMap(createdTournament));
        } catch (Exception e) {
            log.error("Ошибка при создании турнира", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Не удалось создать турнир: " + e.getMessage()));
        }
    }

    /**
     * Создание бизнес-турнира (для администраторов и бизнес-пользователей)
     */
    @PostMapping("/business")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BUSINESS')")
    public ResponseEntity<?> createBusinessTournament(
            @RequestParam(value = "title") String title,
            @RequestParam(value = "date") String dateStr,
            @RequestParam(value = "location") String location,
            @RequestParam(value = "level") String level,
            @RequestParam(value = "prize_pool") Integer prizePool,
            @RequestParam(value = "sponsor_name", required = false) String sponsorName,
            @RequestParam(value = "business_type", required = false) String businessType,
            @RequestParam(value = "tournament_image", required = false) MultipartFile tournamentImage,
            @RequestParam(value = "sponsor_logo", required = false) MultipartFile sponsorLogo) {
        
        try {
            log.info("Получен запрос на создание бизнес-турнира: {}, {}, {}, {}", title, dateStr, location, level);
            
            LocalDate date;
            try {
                // Пробуем разные форматы дат
                if (dateStr.contains("T")) {
                    // Формат ISO с временем
                    date = LocalDateTime.parse(dateStr).toLocalDate();
                } else {
                    // Стандартный формат LocalDate
                    date = LocalDate.parse(dateStr);
                }
            } catch (Exception e) {
                log.error("Ошибка при парсинге даты: {}", dateStr, e);
                return ResponseEntity.badRequest().body(Map.of("error", "Неверный формат даты. Используйте формат YYYY-MM-DD"));
            }
            
            // Сохраняем изображение турнира, если оно было загружено
            String imageUrl = null;
            if (tournamentImage != null && !tournamentImage.isEmpty()) {
                imageUrl = fileStorageService.storeTournamentImage(tournamentImage);
                log.info("Сохранено изображение турнира: {}", imageUrl);
            }
            
            // Сохраняем логотип спонсора, если он был загружен
            String sponsorLogoUrl = null;
            if (sponsorLogo != null && !sponsorLogo.isEmpty()) {
                sponsorLogoUrl = fileStorageService.storeSponsorLogo(sponsorLogo);
                log.info("Сохранен логотип спонсора: {}", sponsorLogoUrl);
            }
            
            // Создаем новый бизнес-турнир
            Tournament tournament = Tournament.builder()
                    .name(title)
                    .date(date)
                    .location(location)
                    .level(level)
                    .prizePool(String.valueOf(prizePool))
                    .status(TournamentStatus.UPCOMING)
                    .imageUrl(imageUrl)
                    .sponsorName(sponsorName)
                    .sponsorLogo(sponsorLogoUrl)
                    .businessType(businessType)
                    .isBusinessTournament(true)
                    .registrationOpen(true) // По умолчанию регистрация открыта
                    .build();
            
            Tournament createdTournament = tournamentService.createTournament(tournament);
            log.info("Бизнес-турнир успешно создан с ID: {}", createdTournament.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToMap(createdTournament));
        } catch (Exception e) {
            log.error("Ошибка при создании бизнес-турнира", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Не удалось создать турнир: " + e.getMessage()));
        }
    }

    /**
     * Обновление существующего турнира (только для администраторов)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTournament(@PathVariable Long id, @RequestBody Map<String, Object> tournamentData) {
        try {
            Tournament existingTournament = tournamentService.getTournamentById(id);
            
            // Используем Optional для безопасного обновления полей
            Optional.ofNullable(tournamentData.get("title"))
                .map(Object::toString)
                .ifPresent(existingTournament::setName);
            
            // Безопасное обновление даты с поддержкой разных форматов
            Optional.ofNullable(tournamentData.get("date"))
                .map(Object::toString)
                .ifPresent(dateStr -> {
                    try {
                        LocalDate date;
                        if (dateStr.contains("T")) {
                            // Формат ISO с временем
                            date = LocalDateTime.parse(dateStr).toLocalDate();
                        } else {
                            // Стандартный формат LocalDate
                            date = LocalDate.parse(dateStr);
                        }
                        existingTournament.setDate(date);
                    } catch (Exception e) {
                        log.error("Ошибка при парсинге даты: {}", dateStr, e);
                        // В данном случае просто логируем ошибку и не обновляем поле
                    }
                });
            
            Optional.ofNullable(tournamentData.get("location"))
                .map(Object::toString)
                .ifPresent(existingTournament::setLocation);
            
            Optional.ofNullable(tournamentData.get("level"))
                .map(Object::toString)
                .ifPresent(existingTournament::setLevel);
            
            Optional.ofNullable(tournamentData.get("prize_pool"))
                .map(Object::toString)
                .ifPresent(existingTournament::setPrizePool);
            
            Optional.ofNullable(tournamentData.get("status"))
                .map(Object::toString)
                .map(status -> {
                    try {
                        return TournamentStatus.valueOf(status.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.error("Неверный статус турнира: {}", status, e);
                        return null;
                    }
                })
                .ifPresent(existingTournament::setStatus);
            
            Optional.ofNullable(tournamentData.get("registration_open"))
                .map(value -> Boolean.valueOf(value.toString()))
                .ifPresent(existingTournament::setRegistrationOpen);
            
            // Обработка полей бизнес-турнира
            Optional.ofNullable(tournamentData.get("sponsor_name"))
                .map(Object::toString)
                .ifPresent(existingTournament::setSponsorName);
            
            Optional.ofNullable(tournamentData.get("sponsor_logo"))
                .map(Object::toString)
                .ifPresent(existingTournament::setSponsorLogo);
            
            Optional.ofNullable(tournamentData.get("business_type"))
                .map(Object::toString)
                .ifPresent(existingTournament::setBusinessType);
            
            Tournament updatedTournament = tournamentService.updateTournament(existingTournament);
            return ResponseEntity.ok(convertToMap(updatedTournament));
        } catch (Exception e) {
            log.error("Ошибка при обновлении турнира с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Не удалось обновить турнир: " + e.getMessage()));
        }
    }

    /**
     * Удаление турнира (только для администраторов)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTournament(@PathVariable Long id) {
        try {
            tournamentService.deleteTournament(id);
            return ResponseEntity.ok().body(Map.of("message", "Турнир успешно удален"));
        } catch (Exception e) {
            log.error("Ошибка при удалении турнира с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Не удалось удалить турнир: " + e.getMessage()));
        }
    }

    /**
     * Регистрация команды на турнир
     */
    @PostMapping("/{id}/register")
    public ResponseEntity<?> registerTeamForTournament(
            @PathVariable("id") Long tournamentId,
            @Valid @RequestBody TeamRegistrationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Для регистрации команды необходимо авторизоваться"));
            }
            
            // Получаем текущего пользователя как капитана
            User captain = userService.getUserByEmail(userDetails.getUsername());
            
            // Получаем турнир
            Tournament tournament = tournamentService.getTournamentById(tournamentId);
            
            // Проверяем, что регистрация на турнир открыта
            if (tournament.getRegistrationOpen() != null && !tournament.getRegistrationOpen()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Регистрация на этот турнир закрыта"));
            }
            
            // Проверяем статус турнира
            String status = tournament.getStatus() != null ? tournament.getStatus().toString().toUpperCase() : "";
            if (!status.equals("UPCOMING") && !status.equals("REGISTRATION")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Регистрация на этот турнир закрыта"));
            }
            
            // Проверяем, что ID турнира в пути совпадает с ID в запросе
            if (!tournamentId.equals(request.getTournamentId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID турнира в пути и в теле запроса не совпадают"));
            }
            
            // Проверяем, что капитан включен в список игроков
            if (!request.getPlayerIds().contains(captain.getId())) {
                request.getPlayerIds().add(captain.getId());
            }
            
            // Проверяем минимальное количество игроков
            if (request.getPlayerIds().size() < 3) {
                return ResponseEntity.badRequest().body(Map.of("error", "В команде должно быть минимум 3 игрока"));
            }
            
            // Создаем регистрацию
            Registration createdRegistration = registrationService.createRegistration(
                tournamentId,
                request.getTeamName(),
                captain.getId(),
                request.getPlayerIds()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", createdRegistration.getId(),
                "teamName", createdRegistration.getTeamName(),
                "status", createdRegistration.getStatus(),
                "message", "Команда успешно зарегистрирована на турнир"
            ));
        } catch (Exception e) {
            log.error("Ошибка при регистрации команды", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Не удалось зарегистрировать команду: " + e.getMessage()));
        }
    }

    /**
     * Обновление данных матча турнира
     */
    @RequestMapping(
            value = "/{tournamentId}/matches/{matchId}", 
            method = RequestMethod.PUT, 
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateMatch(
            @PathVariable Long tournamentId,
            @PathVariable String matchId,
            @RequestBody Map<String, Object> matchData) {
        
        try {
            // Проверяем существование турнира
            Tournament tournament = tournamentService.getTournamentById(tournamentId);
            
            // В реальной реализации здесь была бы логика обновления данных матча в БД
            // TODO: Реализовать обработку обновления матча через специальный сервис
            
            Map<String, Object> updatedMatch = new HashMap<>();
            updatedMatch.put("id", matchId);
            updatedMatch.put("tournamentId", tournamentId);
            updatedMatch.put("score1", matchData.get("score1"));
            updatedMatch.put("score2", matchData.get("score2"));
            updatedMatch.put("isCompleted", matchData.get("isCompleted"));
            updatedMatch.put("updatedAt", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(updatedMatch);
        } catch (Exception e) {
            log.error("Ошибка при обновлении матча", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Не удалось обновить матч: " + e.getMessage()));
        }
    }

    /**
     * Тестовый метод для проверки работы контроллера
     */
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        log.info("Вызван тестовый эндпоинт /tournaments/test");
        
        Map<String, Object> testTournament = new HashMap<>();
        testTournament.put("id", 999L);
        testTournament.put("name", "Тестовый турнир");
        testTournament.put("title", "Тестовый турнир");
        testTournament.put("date", LocalDateTime.now().toString());
        testTournament.put("location", "Тестовая локация");
        testTournament.put("level", "Любительский");
        testTournament.put("prize_pool", 100000);
        testTournament.put("status", "registration");
        testTournament.put("image_url", "/uploads/sponsors/test.jpg");
        testTournament.put("registrations", new ArrayList<>());
        
        List<Map<String, Object>> testData = new ArrayList<>();
        testData.add(testTournament);
        
        return ResponseEntity.ok(testData);
    }

    /**
     * Получение списка команд, зарегистрированных на турнир
     */
    @GetMapping("/{id}/teams")
    public ResponseEntity<?> getTournamentTeams(@PathVariable Long id) {
        try {
            log.info("Получен запрос на получение команд для турнира с ID: {}", id);
            
            Tournament tournament = tournamentService.getTournamentWithTeams(id);
            
            if (tournament.getTeams() == null || tournament.getTeams().isEmpty()) {
                log.info("Команды для турнира с ID: {} не найдены", id);
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            List<Map<String, Object>> result = tournament.getTeams().stream()
                .map(tournamentTeam -> {
                    Map<String, Object> teamMap = new HashMap<>();
                    Team team = tournamentTeam.getTeam();
                    
                    teamMap.put("id", team.getId());
                    teamMap.put("name", team.getName());
                    teamMap.put("logo", team.getLogo());
                    teamMap.put("status", tournamentTeam.getStatus().toString());
                    teamMap.put("position", tournamentTeam.getPosition());
                    teamMap.put("registration_date", tournamentTeam.getRegistrationDate());
                    
                    // Добавляем информацию об игроках команды
                    if (team.getPlayers() != null && !team.getPlayers().isEmpty()) {
                        List<Map<String, Object>> playersList = team.getPlayers().stream()
                            .map(player -> {
                                Map<String, Object> playerMap = new HashMap<>();
                                playerMap.put("id", player.getId());
                                playerMap.put("name", player.getName());
                                playerMap.put("photo", player.getPhotoUrl());
                                return playerMap;
                            })
                            .collect(Collectors.toList());
                        
                        teamMap.put("players", playersList);
                    } else {
                        teamMap.put("players", new ArrayList<>());
                    }
                    
                    return teamMap;
                })
                .collect(Collectors.toList());
            
            log.info("Возвращается {} команд для турнира с ID: {}", result.size(), id);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Ошибка при получении команд для турнира с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при получении команд турнира: " + e.getMessage()));
        }
    }

    /**
     * Подтверждение участия команды в турнире
     */
    @PostMapping("/{tournamentId}/teams/{teamId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> confirmTeamForTournament(
            @PathVariable("tournamentId") Long tournamentId,
            @PathVariable("teamId") Long teamId) {
        
        try {
            log.info("Получен запрос на подтверждение команды с ID {} для турнира с ID {}", teamId, tournamentId);
            
            // Получаем турнир с командами
            Tournament tournament = tournamentService.getTournamentWithTeams(tournamentId);
            
            // Ищем нужную связь команды с турниром
            Optional<TournamentTeam> tournamentTeamOpt = tournament.getTeams().stream()
                .filter(tt -> tt.getTeam().getId().equals(teamId))
                .findFirst();
            
            if (tournamentTeamOpt.isEmpty()) {
                log.warn("Команда с ID {} не найдена в турнире с ID {}", teamId, tournamentId);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Команда не найдена в этом турнире"));
            }
            
            TournamentTeam tournamentTeam = tournamentTeamOpt.get();
            
            // Изменяем статус на APPROVED
            tournamentTeam.setStatus(TeamStatus.APPROVED);
            
            // Сохраняем изменения
            tournamentService.saveTournamentTeam(tournamentTeam);
            
            log.info("Команда с ID {} успешно подтверждена для турнира с ID {}", teamId, tournamentId);
            
            // Возвращаем обновленные данные команды
            Map<String, Object> result = new HashMap<>();
            result.put("id", teamId);
            result.put("tournament_id", tournamentId);
            result.put("status", "CONFIRMED");
            result.put("message", "Команда успешно подтверждена для участия в турнире");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Ошибка при подтверждении команды с ID {} для турнира с ID {}: {}", 
                    teamId, tournamentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при подтверждении команды: " + e.getMessage()));
        }
    }
    
    /**
     * Отклонение участия команды в турнире
     */
    @PostMapping("/{tournamentId}/teams/{teamId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectTeamForTournament(
            @PathVariable("tournamentId") Long tournamentId,
            @PathVariable("teamId") Long teamId) {
        
        try {
            log.info("Получен запрос на отклонение команды с ID {} для турнира с ID {}", teamId, tournamentId);
            
            // Получаем турнир с командами
            Tournament tournament = tournamentService.getTournamentWithTeams(tournamentId);
            
            // Ищем нужную связь команды с турниром
            Optional<TournamentTeam> tournamentTeamOpt = tournament.getTeams().stream()
                .filter(tt -> tt.getTeam().getId().equals(teamId))
                .findFirst();
            
            if (tournamentTeamOpt.isEmpty()) {
                log.warn("Команда с ID {} не найдена в турнире с ID {}", teamId, tournamentId);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Команда не найдена в этом турнире"));
            }
            
            TournamentTeam tournamentTeam = tournamentTeamOpt.get();
            
            // Изменяем статус на REJECTED
            tournamentTeam.setStatus(TeamStatus.REJECTED);
            
            // Сохраняем изменения
            tournamentService.saveTournamentTeam(tournamentTeam);
            
            log.info("Команда с ID {} успешно отклонена для турнира с ID {}", teamId, tournamentId);
            
            // Возвращаем обновленные данные команды
            Map<String, Object> result = new HashMap<>();
            result.put("id", teamId);
            result.put("tournament_id", tournamentId);
            result.put("status", TeamStatus.REJECTED.toString());
            result.put("message", "Команда отклонена для участия в турнире");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Ошибка при отклонении команды с ID {} для турнира с ID {}: {}", 
                    teamId, tournamentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при отклонении команды: " + e.getMessage()));
        }
    }

    /**
     * Преобразование объекта Tournament в Map для возврата клиенту
     */
    private Map<String, Object> convertToMap(Tournament tournament) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", tournament.getId());
        result.put("name", tournament.getName());
        result.put("title", tournament.getName());
        result.put("date", tournament.getDate().toString());
        result.put("location", tournament.getLocation());
        result.put("level", tournament.getLevel());
        result.put("prize_pool", tournament.getPrizePool());
        result.put("status", tournament.getStatus() != null ? tournament.getStatus().toString() : null);
        
        // Явно указываем Boolean.TRUE, если registrationOpen равен null или true
        Boolean isRegistrationOpen = tournament.getRegistrationOpen() == null ? Boolean.TRUE : tournament.getRegistrationOpen();
        result.put("registration_open", isRegistrationOpen);
        
        // Добавляем URL изображения, если оно есть
        if (tournament.getImageUrl() != null) {
            result.put("image_url", tournament.getImageUrl());
        } else if (tournament.getSponsorLogo() != null) {
            // Используем логотип спонсора в качестве изображения, если нет основного изображения
            result.put("image_url", tournament.getSponsorLogo());
        }
        
        // Добавляем бизнес-поля, если они есть
        if (tournament.getSponsorName() != null) {
            result.put("sponsor_name", tournament.getSponsorName());
        }
        
        if (tournament.getSponsorLogo() != null) {
            result.put("sponsor_logo", tournament.getSponsorLogo());
        }
        
        if (tournament.getBusinessType() != null) {
            result.put("business_type", tournament.getBusinessType());
        }
        
        // Добавляем информацию о регистрациях команд
        if (tournament.getRegistrations() != null && !tournament.getRegistrations().isEmpty()) {
            List<Map<String, Object>> registrations = tournament.getRegistrations().stream()
                .map(registration -> {
                    Map<String, Object> regMap = new HashMap<>();
                    regMap.put("id", registration.getId());
                    regMap.put("team_name", registration.getTeamName());
                    regMap.put("status", registration.getStatus());
                    
                    // Добавляем информацию о капитане
                    if (registration.getCaptain() != null) {
                        regMap.put("captain_id", registration.getCaptain().getId());
                        regMap.put("captain_name", registration.getCaptain().getName());
                    }
                    
                    // Добавляем информацию об игроках
                    if (registration.getPlayers() != null && !registration.getPlayers().isEmpty()) {
                        List<Map<String, Object>> players = registration.getPlayers().stream()
                            .map(player -> {
                                Map<String, Object> playerMap = new HashMap<>();
                                playerMap.put("id", player.getId());
                                playerMap.put("name", player.getName());
                                if (registration.getCaptain() != null) {
                                    playerMap.put("is_captain", player.getId().equals(registration.getCaptain().getId()));
                                }
                                return playerMap;
                            })
                            .collect(Collectors.toList());
                        regMap.put("players", players);
                    }
                    
                    return regMap;
                })
                .collect(Collectors.toList());
            
            result.put("registrations", registrations);
        } else {
            result.put("registrations", new ArrayList<>());
        }
        
        return result;
    }
} 