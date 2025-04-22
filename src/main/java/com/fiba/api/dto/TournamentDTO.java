package com.fiba.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TournamentDTO {
    private Long id;
    private String name;
    private String title;
    private LocalDateTime date;
    private String location;
    private String level;
    private Integer prizePool;
    private String status;
    private String sponsorName;
    private String sponsorLogo;
    private String imageUrl;
    private String businessType;
    private List<RegistrationDTO> registrations;
} 