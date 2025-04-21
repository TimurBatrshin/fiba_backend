package com.fiba.api.controller;

import com.fiba.api.dto.TournamentRequest;
import com.fiba.api.dto.TeamStatusRequest;
import com.fiba.api.model.Tournament;
import com.fiba.api.service.TournamentService;
import com.fiba.api.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.HashMap;

/**
 * Контроллер для административных функций
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TournamentService tournamentService;
    private final TeamService teamService;

    /**
     * Создание нового турнира
     * 
     * @param request данные турнира
     * @return созданный турнир
     */
    @PostMapping("/tournaments")
    public ResponseEntity<Tournament> createTournament(@Valid @RequestBody TournamentRequest request) {
        Tournament tournament = tournamentService.createTournament(request);
        return ResponseEntity.ok(tournament);
    }

    /**
     * Обновление турнира
     * 
     * @param id идентификатор турнира
     * @param request данные для обновления
     * @return обновленный турнир
     */
    @PutMapping("/tournaments/{id}")
    public ResponseEntity<Tournament> updateTournament(
            @PathVariable Long id, 
            @Valid @RequestBody TournamentRequest request) {
        Tournament tournament = tournamentService.updateTournament(id, request);
        return ResponseEntity.ok(tournament);
    }

    /**
     * Удаление турнира
     * 
     * @param id идентификатор турнира
     * @return статус операции
     */
    @DeleteMapping("/tournaments/{id}")
    public ResponseEntity<Map<String, String>> deleteTournament(@PathVariable Long id) {
        tournamentService.deleteTournament(id);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Турнир успешно удален");
        return ResponseEntity.ok(response);
    }

    /**
     * Изменение статуса команды в турнире (подтверждение или отклонение)
     * 
     * @param tournamentId идентификатор турнира
     * @param teamId идентификатор команды
     * @param request новый статус команды
     * @return результат операции
     */
    @PutMapping("/tournaments/{tournamentId}/teams/{teamId}")
    public ResponseEntity<Map<String, String>> updateTeamStatus(
            @PathVariable Long tournamentId,
            @PathVariable Long teamId,
            @Valid @RequestBody TeamStatusRequest request) {
        teamService.updateTeamStatus(tournamentId, teamId, request.getStatus());
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Статус команды успешно обновлен");
        return ResponseEntity.ok(response);
    }
} 