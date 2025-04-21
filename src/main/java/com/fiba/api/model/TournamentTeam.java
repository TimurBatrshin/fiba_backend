package com.fiba.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Модель данных для связи команды с турниром
 */
@Entity
@Table(name = "tournament_teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamStatus status;

    private Integer position; // Финальная позиция команды в турнире

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @PrePersist
    protected void onCreate() {
        this.registrationDate = LocalDateTime.now();
    }
} 