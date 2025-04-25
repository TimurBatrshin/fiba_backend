package com.fiba.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TeamRegistrationRequest {
    @NotNull(message = "ID турнира обязателен")
    private Long tournamentId;

    @NotBlank(message = "Название команды обязательно")
    @Size(min = 3, max = 100, message = "Название команды должно содержать от 3 до 100 символов")
    private String teamName;

    @Size(min = 1, message = "Список игроков не может быть пустым")
    private List<Long> playerIds;
} 