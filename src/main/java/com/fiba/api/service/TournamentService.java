package com.fiba.api.service;

import com.fiba.api.dto.TournamentRequest;
import com.fiba.api.exception.ResourceNotFoundException;
import com.fiba.api.model.Tournament;
import com.fiba.api.model.TournamentStatus;
import com.fiba.api.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с турнирами
 */
@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;

    /**
     * Получение всех турниров
     * 
     * @return список всех турниров
     */
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    /**
     * Получение турнира по идентификатору
     * 
     * @param id идентификатор турнира
     * @return турнир
     * @throws ResourceNotFoundException если турнир не найден
     */
    public Tournament getTournamentById(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + id));
    }

    /**
     * Создание нового турнира
     * 
     * @param request данные турнира
     * @return созданный турнир
     */
    @Transactional
    public Tournament createTournament(TournamentRequest request) {
        Tournament tournament = Tournament.builder()
                .name(request.getName())
                .date(request.getDate())
                .startTime(request.getStartTime())
                .location(request.getLocation())
                .description(request.getDescription())
                .status(request.getStatus())
                .maxTeams(request.getMaxTeams())
                .entryFee(request.getEntryFee())
                .prizePool(request.getPrizePool())
                .isBusinessTournament(request.getIsBusinessTournament())
                .sponsorName(request.getSponsorName())
                .sponsorLogo(request.getSponsorLogo())
                .rules(request.getRules())
                .registrationOpen(request.getRegistrationOpen())
                .build();
        
        return tournamentRepository.save(tournament);
    }

    /**
     * Создание нового турнира напрямую из объекта Tournament
     * 
     * @param tournament объект турнира для создания или обновления
     * @return сохраненный турнир
     */
    @Transactional
    public Tournament createTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    /**
     * Обновление турнира
     * 
     * @param id идентификатор турнира
     * @param request данные для обновления
     * @return обновленный турнир
     * @throws ResourceNotFoundException если турнир не найден
     */
    @Transactional
    public Tournament updateTournament(Long id, TournamentRequest request) {
        Tournament tournament = getTournamentById(id);
        
        tournament.setName(request.getName());
        tournament.setDate(request.getDate());
        tournament.setStartTime(request.getStartTime());
        tournament.setLocation(request.getLocation());
        tournament.setDescription(request.getDescription());
        tournament.setStatus(request.getStatus());
        tournament.setMaxTeams(request.getMaxTeams());
        tournament.setEntryFee(request.getEntryFee());
        tournament.setPrizePool(request.getPrizePool());
        tournament.setIsBusinessTournament(request.getIsBusinessTournament());
        tournament.setSponsorName(request.getSponsorName());
        tournament.setSponsorLogo(request.getSponsorLogo());
        tournament.setRules(request.getRules());
        tournament.setRegistrationOpen(request.getRegistrationOpen());
        
        return tournamentRepository.save(tournament);
    }

    /**
     * Удаление турнира
     * 
     * @param id идентификатор турнира
     * @throws ResourceNotFoundException если турнир не найден
     */
    @Transactional
    public void deleteTournament(Long id) {
        if (!tournamentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tournament not found with id: " + id);
        }
        tournamentRepository.deleteById(id);
    }

    /**
     * Получение предстоящих турниров
     * 
     * @return список предстоящих турниров
     */
    public List<Tournament> getUpcomingTournaments() {
        LocalDate today = LocalDate.now();
        return tournamentRepository.findByDateGreaterThanEqualAndStatus(today, TournamentStatus.UPCOMING);
    }

    /**
     * Получение завершенных турниров
     * 
     * @return список завершенных турниров
     */
    public List<Tournament> getCompletedTournaments() {
        LocalDate today = LocalDate.now();
        return tournamentRepository.findByDateLessThanAndStatus(today, TournamentStatus.COMPLETED);
    }

    /**
     * Поиск турниров по местоположению
     * 
     * @param location местоположение
     * @return список турниров в указанном месте
     */
    public List<Tournament> getTournamentsByLocation(String location) {
        return tournamentRepository.findByLocationContainingIgnoreCase(location);
    }

    /**
     * Получение бизнес-турниров
     * 
     * @return список бизнес-турниров
     */
    public List<Tournament> getBusinessTournaments() {
        return tournamentRepository.findByIsBusinessTournament(true);
    }
    
    /**
     * Получение турниров по статусу
     * 
     * @param status статус турнира
     * @return список турниров с указанным статусом
     */
    public List<Tournament> getTournamentsByStatus(String status) {
        try {
            TournamentStatus tournamentStatus = TournamentStatus.valueOf(status.toUpperCase());
            return tournamentRepository.findByStatus(tournamentStatus);
        } catch (IllegalArgumentException e) {
            // Если статус не удается преобразовать в enum, возвращаем пустой список
            return new ArrayList<>();
        }
    }

    /**
     * Поиск турниров по запросу
     * 
     * @param query поисковый запрос
     * @return список найденных турниров
     */
    public List<Tournament> searchTournaments(String query) {
        // Можно реализовать более сложную логику поиска
        // Пока ищем по названию
        return tournamentRepository.searchByTitle(query);
    }
    
    /**
     * Получение турниров по уровню
     * 
     * @param level уровень турнира
     * @return список турниров с указанным уровнем
     */
    public List<Tournament> getTournamentsByLevel(String level) {
        return tournamentRepository.findByLevel(level);
    }
    
    /**
     * Обновление турнира
     * 
     * @param tournament турнир для обновления
     * @return обновленный турнир
     */
    @Transactional
    public Tournament updateTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }
} 