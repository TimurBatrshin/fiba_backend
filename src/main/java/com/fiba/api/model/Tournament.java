package com.fiba.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Модель данных для турнира
 */
@Entity
@Table(name = "tournaments", indexes = {
    @Index(name = "idx_tournament_date", columnList = "date"),
    @Index(name = "idx_tournament_status", columnList = "status"),
    @Index(name = "idx_tournament_level", columnList = "level")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    private LocalTime startTime;

    @Column(nullable = false)
    private String location;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status;

    @Column(name = "level")
    private String level;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "business_type")
    private String businessType;

    private Integer maxTeams;

    private Double entryFee;

    private String prizePool;

    private Boolean isBusinessTournament;

    private String sponsorName;

    private String sponsorLogo;

    @Column(length = 1000)
    private String rules;

    private Boolean registrationOpen;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TournamentTeam> teams = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Registration> registrations = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Ad> ads = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Добавление команды в турнир
     * 
     * @param team команда
     * @return запись об участии команды в турнире
     */
    public TournamentTeam addTeam(Team team) {
        TournamentTeam tournamentTeam = new TournamentTeam();
        tournamentTeam.setTournament(this);
        tournamentTeam.setTeam(team);
        tournamentTeam.setStatus(TeamStatus.PENDING);
        this.teams.add(tournamentTeam);
        return tournamentTeam;
    }
} 