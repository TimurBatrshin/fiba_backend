package com.fiba.api.repository;

import com.fiba.api.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с командами
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * Поиск команд по имени (частичное совпадение, без учета регистра)
     * 
     * @param name часть имени команды
     * @return список найденных команд
     */
    List<Team> findByNameContainingIgnoreCase(String name);

    /**
     * Поиск топ команд по рейтингу
     * 
     * @param limit максимальное количество команд
     * @return список топ команд
     */
    @Query("SELECT t FROM Team t ORDER BY t.totalPoints DESC")
    List<Team> findTopTeamsByRating(int limit);
} 