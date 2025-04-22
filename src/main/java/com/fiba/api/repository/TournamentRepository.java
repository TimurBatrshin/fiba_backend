package com.fiba.api.repository;

import com.fiba.api.model.Tournament;
import com.fiba.api.model.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с турнирами
 */
@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    /**
     * Поиск предстоящих турниров
     * 
     * @param date дата, после которой искать турниры
     * @param status статус турниров
     * @return список предстоящих турниров
     */
    List<Tournament> findByDateGreaterThanEqualAndStatus(LocalDate date, TournamentStatus status);

    /**
     * Поиск прошедших турниров
     * 
     * @param date дата, до которой искать турниры
     * @param status статус турниров
     * @return список прошедших турниров
     */
    List<Tournament> findByDateLessThanAndStatus(LocalDate date, TournamentStatus status);

    /**
     * Поиск турниров по местоположению
     * 
     * @param location местоположение
     * @return список турниров в указанном месте
     */
    List<Tournament> findByLocationContainingIgnoreCase(String location);

    /**
     * Поиск бизнес-турниров
     * 
     * @param isBusinessTournament флаг бизнес-турнира
     * @return список бизнес-турниров
     */
    List<Tournament> findByIsBusinessTournament(Boolean isBusinessTournament);

    List<Tournament> findByStatus(String status);
    
    List<Tournament> findByDateAfter(LocalDate date);
    
    List<Tournament> findByDateBefore(LocalDate date);
    
    @Query("SELECT t FROM Tournament t WHERE t.level = :level")
    List<Tournament> findByLevel(String level);
    
    @Query("SELECT t FROM Tournament t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Tournament> searchByTitle(String searchTerm);
    
    /**
     * Получить турнир по ID с загрузкой всех регистраций команд
     * @param id ID турнира
     * @return турнир с загруженными регистрациями
     */
    @Query("SELECT DISTINCT t FROM Tournament t LEFT JOIN FETCH t.registrations r LEFT JOIN FETCH r.captain WHERE t.id = :id")
    Optional<Tournament> findByIdWithRegistrations(@Param("id") Long id);
} 