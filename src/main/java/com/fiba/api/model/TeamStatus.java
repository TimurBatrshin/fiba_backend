package com.fiba.api.model;

/**
 * Перечисление возможных статусов команды в турнире
 */
public enum TeamStatus {
    PENDING,    // Ожидает подтверждения
    APPROVED,   // Подтверждена
    REJECTED,   // Отклонена
    COMPLETED   // Завершила участие
} 