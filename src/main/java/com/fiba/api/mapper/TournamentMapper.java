package com.fiba.api.mapper;

import com.fiba.api.dto.TournamentDTO;
import com.fiba.api.model.Tournament;
import com.fiba.api.model.TournamentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TournamentMapper {
    
    public TournamentDTO toDTO(Tournament tournament) {
        if (tournament == null) {
            return null;
        }
        
        return TournamentDTO.builder()
                .id(tournament.getId())
                .name(tournament.getName()) // Assuming name maps to title
                .title(tournament.getDescription())
                .date(tournament.getDate().atStartOfDay())
                .location(tournament.getLocation())
                .level(tournament.getLevel())
                .prizePool(Integer.valueOf(tournament.getPrizePool()))
                .status(String.valueOf(tournament.getStatus()))
                .sponsorName(tournament.getSponsorName())
                .sponsorLogo(tournament.getSponsorLogo())
                .imageUrl(tournament.getImageUrl())
                .businessType(tournament.getBusinessType())
                .build();
    }
    
    public Tournament toEntity(TournamentDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return Tournament.builder()
                .id(dto.getId())
                .name(dto.getTitle())
                .date(LocalDate.from(dto.getDate()))
                .location(dto.getLocation())
                .level(dto.getLevel())
                .prizePool(String.valueOf(dto.getPrizePool()))
                .status(TournamentStatus.valueOf(dto.getStatus()))
                .sponsorName(dto.getSponsorName())
                .sponsorLogo(dto.getSponsorLogo())
                .imageUrl(dto.getImageUrl())
                .businessType(dto.getBusinessType())
                .build();
    }
} 