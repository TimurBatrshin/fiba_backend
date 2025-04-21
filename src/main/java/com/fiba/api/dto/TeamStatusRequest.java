package com.fiba.api.dto;

import com.fiba.api.model.TeamStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO для обновления статуса команды
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatusRequest {

    @NotNull(message = "Статус команды обязателен")
    private TeamStatus status;
} 