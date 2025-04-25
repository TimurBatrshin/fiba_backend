package com.fiba.api.repository;

import com.fiba.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Поиск пользователя по email (без учета регистра)
     * @param email email пользователя
     * @return Optional с найденным пользователем или пустой Optional
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmail(@Param("email") String email);
    
    /**
     * Проверка существования пользователя с указанным email (без учета регистра)
     * @param email email для проверки
     * @return true если пользователь существует, false в противном случае
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * Поиск пользователей по имени или email, содержащим искомую строку (без учета регистра)
     * @param query строка для поиска
     * @return список пользователей, соответствующих критериям поиска
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchByNameOrEmail(@Param("query") String query);

    /**
     * Найти пользователей по списку ID
     * @param ids список ID пользователей
     * @return список найденных пользователей
     */
    List<User> findByIdIn(List<Long> ids);
} 