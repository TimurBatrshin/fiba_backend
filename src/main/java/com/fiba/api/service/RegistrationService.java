package com.fiba.api.service;

import com.fiba.api.model.*;
import com.fiba.api.repository.RegistrationRepository;
import com.fiba.api.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final TournamentService tournamentService;
    private final UserService userService;
    private final TeamService teamService;
    private final TeamRepository teamRepository;
    private final PlayerService playerService;

    @Transactional
    public Registration createRegistration(Long tournamentId, String teamName, Long captainId, List<Long> playerIds) {
        Tournament tournament = tournamentService.getTournamentById(tournamentId);
        User captain = userService.getUserById(captainId);
        List<User> users = userService.getUsersByIds(playerIds);
        
        // Convert users to players
        List<Player> players = playerService.getOrCreatePlayers(users);

        // Создаем команду
        Team team = new Team();
        team.setName(teamName);
        team.setPlayers(players);
        team = teamRepository.save(team);

        // Создаем регистрацию
        Registration registration = new Registration();
        registration.setTeamName(teamName);
        registration.setTournament(tournament);
        registration.setCaptain(captain);
        registration.setPlayers(users); // Registration still uses Users
        registration.setStatus("pending");

        // Связываем команду с турниром через TournamentTeam
        TournamentTeam tournamentTeam = new TournamentTeam();
        tournamentTeam.setTeam(team);
        tournamentTeam.setTournament(tournament);
        tournamentTeam.setStatus(TeamStatus.PENDING);
        tournament.getTeams().add(tournamentTeam);

        return registrationRepository.save(registration);
    }

    @Transactional(readOnly = true)
    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Registration getRegistrationById(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Регистрация с ID " + id + " не найдена"));
    }

    @Transactional(readOnly = true)
    public List<Registration> getRegistrationsByTournament(Long tournamentId) {
        return registrationRepository.findByTournamentId(tournamentId);
    }

    @Transactional(readOnly = true)
    public List<Registration> getRegistrationsByCaptain(Long captainId) {
        User captain = userService.getUserById(captainId);
        return registrationRepository.findByCaptain(captain);
    }

    @Transactional(readOnly = true)
    public List<Registration> getTeamsByPlayer(Long playerId) {
        return registrationRepository.findByPlayerId(playerId);
    }

    @Transactional(readOnly = true)
    public List<Registration> getRegistrationsByTournamentAndStatus(Long tournamentId, String status) {
        Tournament tournament = tournamentService.getTournamentById(tournamentId);
        return registrationRepository.findByTournamentAndStatus(tournament, status);
    }

    @Transactional
    public Registration updateRegistration(Registration registration) {
        Registration existingRegistration = registrationRepository.findById(registration.getId())
                .orElseThrow(() -> new RuntimeException("Регистрация с ID " + registration.getId() + " не найдена"));
        
        if (registration.getTeamName() != null) {
            existingRegistration.setTeamName(registration.getTeamName());
        }
        
        if (registration.getStatus() != null) {
            existingRegistration.setStatus(registration.getStatus());
        }
        
        if (!registration.getPlayers().isEmpty()) {
            existingRegistration.setPlayers(registration.getPlayers());
        }
        
        return registrationRepository.save(existingRegistration);
    }

    @Transactional
    public Registration updateRegistrationStatus(Long registrationId, String status) {
        Registration existingRegistration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Регистрация с ID " + registrationId + " не найдена"));
        
        existingRegistration.setStatus(status);
        return registrationRepository.save(existingRegistration);
    }

    @Transactional
    public Registration addPlayerToTeam(Long registrationId, Long playerId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Регистрация с ID " + registrationId + " не найдена"));
        
        User player = userService.getUserById(playerId);
        
        if (!registration.getPlayers().contains(player)) {
            registration.getPlayers().add(player);
        }
        
        return registrationRepository.save(registration);
    }

    @Transactional
    public Registration removePlayerFromTeam(Long registrationId, Long playerId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Регистрация с ID " + registrationId + " не найдена"));
        
        User player = userService.getUserById(playerId);
        
        // Нельзя удалить капитана из команды
        if (player.equals(registration.getCaptain())) {
            throw new RuntimeException("Нельзя удалить капитана из команды");
        }
        
        registration.getPlayers().remove(player);
        return registrationRepository.save(registration);
    }

    @Transactional
    public void deleteRegistration(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Регистрация с ID " + id + " не найдена"));
        
        registrationRepository.delete(registration);
    }

    /**
     * Получает регистрацию с игроками по ID
     * @param id ID регистрации
     * @return Регистрация с загруженными игроками
     */
    @Transactional(readOnly = true)
    public Registration getRegistrationWithPlayersById(Long id) {
        return registrationRepository.loadRegistrationWithPlayers(id)
                .orElseThrow(() -> new RuntimeException("Регистрация с ID " + id + " не найдена"));
    }
} 