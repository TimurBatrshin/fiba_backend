package com.fiba.api.service;

import com.fiba.api.dto.TournamentRequest;
import com.fiba.api.exception.ResourceNotFoundException;
import com.fiba.api.model.Team;
import com.fiba.api.model.Tournament;
import com.fiba.api.model.TournamentStatus;
import com.fiba.api.model.TournamentTeam;
import com.fiba.api.model.Registration;
import com.fiba.api.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с турнирами
 */
@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    /**
     * Получение всех турниров
     *
     * @param sort поле для сортировки (например, "date", "name")
     * @param direction направление сортировки ("asc" или "desc")
     * @return список всех турниров, отсортированный согласно параметрам
     */
    public List<Tournament> getAllTournaments(String sort, String direction) {
        if (sort == null || sort.isEmpty()) {
            return tournamentRepository.findAll();
        }
        
        // Определяем направление сортировки
        boolean isAscending = direction == null || "asc".equalsIgnoreCase(direction);
        
        // Получаем все турниры
        List<Tournament> tournaments = tournamentRepository.findAll();
        
        // Сортируем список в соответствии с параметрами
        switch (sort.toLowerCase()) {
            case "date":
                tournaments.sort((t1, t2) -> {
                    int result = t1.getDate().compareTo(t2.getDate());
                    return isAscending ? result : -result;
                });
                break;
            case "name":
                tournaments.sort((t1, t2) -> {
                    int result = t1.getName().compareTo(t2.getName());
                    return isAscending ? result : -result;
                });
                break;
            case "status":
                tournaments.sort((t1, t2) -> {
                    int result = t1.getStatus().name().compareTo(t2.getStatus().name());
                    return isAscending ? result : -result;
                });
                break;
            // Добавьте другие поля для сортировки по необходимости
        }
        
        return tournaments;
    }

    /**
     * Получение всех турниров без сортировки
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

        if (request.getName() != null) {
            tournament.setName(request.getName());
        }
        if (request.getDate() != null) {
            tournament.setDate(request.getDate());
        }
        tournament.setStartTime(request.getStartTime());
        if (request.getLocation() != null) {
            tournament.setLocation(request.getLocation());
        }
        tournament.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            tournament.setStatus(request.getStatus());
        }
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
        if (location == null || location.isEmpty()) {
            return new ArrayList<>();
        }
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
        if (status == null || status.isEmpty()) {
            return new ArrayList<>();
        }
        
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
        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }
        return tournamentRepository.searchByTitle(query);
    }

    /**
     * Получение турниров по уровню
     *
     * @param level уровень турнира
     * @return список турниров с указанным уровнем
     */
    public List<Tournament> getTournamentsByLevel(String level) {
        if (level == null || level.isEmpty()) {
            return new ArrayList<>();
        }
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
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament cannot be null");
        }
        
        if (tournament.getId() == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null for update operation");
        }
        
        // Проверяем существование турнира перед обновлением
        if (!tournamentRepository.existsById(tournament.getId())) {
            throw new ResourceNotFoundException("Tournament not found with id: " + tournament.getId());
        }
        
        return tournamentRepository.save(tournament);
    }

    /**
     * Получение турнира по идентификатору с загрузкой регистраций
     *
     * @param id идентификатор турнира
     * @return турнир с загруженными регистрациями
     * @throws ResourceNotFoundException если турнир не найден
     */
    public Tournament getTournamentWithRegistrations(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null");
        }
        
        return tournamentRepository.findByIdWithRegistrations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + id));
    }

    /**
     * Получение турнира вместе с командами и их игроками
     *
     * @param id идентификатор турнира
     * @return турнир со связанными командами и игроками
     * @throws ResourceNotFoundException если турнир не найден
     */
    @Transactional
    public Tournament getTournamentWithTeams(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Tournament ID cannot be null");
        }
        
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + id));
        
        // Инициализируем коллекцию команд, если она не null
        if (tournament.getTeams() != null) {
            tournament.getTeams().size(); // Инициализация коллекции
            
            // Инициализируем связанные команды и их игроков
            for (TournamentTeam tournamentTeam : tournament.getTeams()) {
                Team team = tournamentTeam.getTeam();
                if (team != null && team.getPlayers() != null) {
                    team.getPlayers().size(); // Инициализация коллекции игроков
                }
            }
        }
        
        return tournament;
    }

    /**
     * Сохранение связи турнира с командой
     *
     * @param tournamentTeam связь турнира с командой
     * @return сохраненная связь
     */
    @Transactional
    public TournamentTeam saveTournamentTeam(TournamentTeam tournamentTeam) {
        if (tournamentTeam == null) {
            throw new IllegalArgumentException("TournamentTeam cannot be null");
        }
        
        // Для простоты используем репозиторий турнира и сохраняем весь турнир с изменениями.
        // В реальном приложении лучше использовать специальный репозиторий для TournamentTeam.
        Tournament tournament = tournamentTeam.getTournament();
        if (tournament != null && tournament.getId() != null) {
            return tournamentRepository.save(tournament)
                    .getTeams().stream()
                    .filter(tt -> tt.getTeam().getId().equals(tournamentTeam.getTeam().getId()))
                    .findFirst()
                    .orElse(tournamentTeam);
        } else {
            throw new IllegalArgumentException("Tournament must be set and have an ID");
        }
    }

    /**
     * Получение команд турнира
     * @param tournamentId ID турнира
     * @return список команд турнира
     */
    @Transactional(readOnly = true)
    public List<Team> getTeamsByTournamentId(Long tournamentId) {
        Tournament tournament = getTournamentById(tournamentId);
        return tournament.getTeams().stream()
                .map(TournamentTeam::getTeam)
                .collect(Collectors.toList());
    }

    /**
     * Get all teams for a tournament with their tournament-specific information
     *
     * @param tournamentId the ID of the tournament
     * @return list of TournamentTeam objects containing team and tournament-specific data
     * @throws ResourceNotFoundException if tournament is not found
     */
    @Transactional(readOnly = true)
    public List<TournamentTeam> getTournamentTeams(Long tournamentId) {
        Tournament tournament = getTournamentById(tournamentId);
        return new ArrayList<>(tournament.getTeams());
    }

    /**
     * Получение регистраций команд турнира
     * @param tournamentId ID турнира
     * @return список регистраций
     */
    @Transactional(readOnly = true)
    public List<Registration> getTournamentRegistrations(Long tournamentId) {
        Tournament tournament = getTournamentById(tournamentId);
        return tournament.getRegistrations();
    }
}