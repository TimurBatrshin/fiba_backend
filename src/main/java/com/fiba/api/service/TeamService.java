package com.fiba.api.service;

import com.fiba.api.exception.ResourceNotFoundException;
import com.fiba.api.model.Team;
import com.fiba.api.model.TeamStatus;
import com.fiba.api.model.Tournament;
import com.fiba.api.model.TournamentTeam;
import com.fiba.api.repository.TeamRepository;
import com.fiba.api.repository.TournamentRepository;
import com.fiba.api.repository.TournamentTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для работы с командами
 */
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository tournamentTeamRepository;

    /**
     * Получение всех команд
     * 
     * @return список всех команд
     */
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    /**
     * Получение команды по идентификатору
     * 
     * @param id идентификатор команды
     * @return команда
     * @throws ResourceNotFoundException если команда не найдена
     */
    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
    }

    /**
     * Поиск команд по имени
     * 
     * @param name имя или часть имени команды
     * @return список найденных команд
     */
    public List<Team> searchTeamsByName(String name) {
        return teamRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Получение топ команд по рейтингу
     * 
     * @param limit максимальное количество команд
     * @return список топ команд
     */
    public List<Team> getTopTeams(int limit) {
        return teamRepository.findTopTeamsByRating(limit);
    }

    /**
     * Получение команд, участвующих в турнире
     * 
     * @param tournamentId идентификатор турнира
     * @return список команд турнира
     */
    public List<TournamentTeam> getTeamsByTournament(Long tournamentId) {
        // Проверяем, существует ли турнир
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament not found with id: " + tournamentId);
        }
        return tournamentTeamRepository.findByTournamentId(tournamentId);
    }

    /**
     * Получение команд турнира по статусу
     * 
     * @param tournamentId идентификатор турнира
     * @param status статус команды
     * @return список команд с указанным статусом
     */
    public List<TournamentTeam> getTeamsByTournamentAndStatus(Long tournamentId, TeamStatus status) {
        // Проверяем, существует ли турнир
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament not found with id: " + tournamentId);
        }
        return tournamentTeamRepository.findByTournamentIdAndStatus(tournamentId, status);
    }

    /**
     * Обновление статуса команды в турнире
     * 
     * @param tournamentId идентификатор турнира
     * @param teamId идентификатор команды
     * @param status новый статус
     * @return обновленная запись
     * @throws ResourceNotFoundException если запись не найдена
     */
    @Transactional
    public TournamentTeam updateTeamStatus(Long tournamentId, Long teamId, TeamStatus status) {
        TournamentTeam tournamentTeam = tournamentTeamRepository.findByTournamentIdAndTeamId(tournamentId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team participation not found for tournament id: " + tournamentId + " and team id: " + teamId));
        
        tournamentTeam.setStatus(status);
        
        // Если статус APPROVED или REJECTED, обновляем статистику команды
        if (status == TeamStatus.APPROVED) {
            Team team = tournamentTeam.getTeam();
            team.setTournamentsPlayed(team.getTournamentsPlayed() + 1);
            teamRepository.save(team);
        }
        
        return tournamentTeamRepository.save(tournamentTeam);
    }

    /**
     * Регистрация команды на турнир
     * 
     * @param tournamentId идентификатор турнира
     * @param teamId идентификатор команды
     * @return запись об участии
     * @throws ResourceNotFoundException если турнир или команда не найдены
     */
    @Transactional
    public TournamentTeam registerTeamForTournament(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + tournamentId));
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
        
        // Проверяем, не зарегистрирована ли уже команда на этот турнир
        if (tournamentTeamRepository.findByTournamentIdAndTeamId(tournamentId, teamId).isPresent()) {
            throw new IllegalStateException("Team is already registered for this tournament");
        }
        
        // Создаем новую запись об участии
        TournamentTeam tournamentTeam = new TournamentTeam();
        tournamentTeam.setTournament(tournament);
        tournamentTeam.setTeam(team);
        tournamentTeam.setStatus(TeamStatus.PENDING);
        
        return tournamentTeamRepository.save(tournamentTeam);
    }
} 