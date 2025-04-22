package com.fiba.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RegistrationDTO {
    private Long id;
    private Long tournamentId;
    private Long teamId;
    private String teamName;
    private String status;
    private LocalDateTime registrationDate;
    private String paymentStatus;
    private Double paymentAmount;
} 