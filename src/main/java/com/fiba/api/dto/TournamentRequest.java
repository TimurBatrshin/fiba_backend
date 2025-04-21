package com.fiba.api.dto;

import com.fiba.api.model.TournamentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO для создания или обновления турнира
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRequest {

    @NotBlank(message = "Название турнира обязательно")
    @Size(max = 100, message = "Название турнира не должно превышать 100 символов")
    private String name;

    @NotNull(message = "Дата турнира обязательна")
    private LocalDate date;

    private LocalTime startTime;

    @NotBlank(message = "Место проведения обязательно")
    private String location;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;
    
    @NotNull(message = "Статус турнира обязателен")
    private TournamentStatus status;
    
    private Integer maxTeams;
    
    private Double entryFee;
    
    private String prizePool;
    
    // Для бизнес-турниров
    private Boolean isBusinessTournament;
    
    private String sponsorName;
    
    private String sponsorLogo;
    
    // Дополнительные поля
    private String rules;
    
    private Boolean registrationOpen;
} 