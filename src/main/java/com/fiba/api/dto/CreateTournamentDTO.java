package com.fiba.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
public class CreateTournamentDTO {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotNull(message = "Date is required")
    private LocalDateTime date;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotBlank(message = "Level is required")
    private String level;
    
    @Positive(message = "Prize pool must be positive")
    private Integer prizePool;
    
    private String sponsorName;
    private String businessType;
} 