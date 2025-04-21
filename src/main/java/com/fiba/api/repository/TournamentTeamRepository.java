package com.fiba.api.repository;

import com.fiba.api.model.TournamentTeam;
import com.fiba.api.model.TeamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы со связями между турнирами и командами
 */
@Repository
public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {

    /**
     * Поиск связи турнира и команды
     * 
     * @param tournamentId идентификатор турнира
     * @param teamId идентификатор команды
     * @return связь турнира и команды
     */
    Optional<TournamentTeam> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);

    /**
     * Поиск команд турнира по статусу
     * 
     * @param tournamentId идентификатор турнира
     * @param status статус команды
     * @return список связей турнира и команд с указанным статусом
     */
    List<TournamentTeam> findByTournamentIdAndStatus(Long tournamentId, TeamStatus status);

    /**
     * Поиск всех команд турнира
     * 
     * @param tournamentId идентификатор турнира
     * @return список всех команд турнира
     */
    List<TournamentTeam> findByTournamentId(Long tournamentId);

    /**
     * Поиск всех турниров команды
     * 
     * @param teamId идентификатор команды
     * @return список всех турниров команды
     */
    List<TournamentTeam> findByTeamId(Long teamId);
} 