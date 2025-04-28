package com.fiba.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlags {
    
    @Id
    private String id = "default";

    @Column(nullable = false)
    private boolean disableBackend = false;

    @Column(nullable = false)
    private boolean showTopPlayers = true;

    @Column(nullable = false)
    private boolean showAdminPanel = true;

    @Column(nullable = false)
    private boolean enableAdminPage = true;

    @Column(nullable = false)
    private boolean enableTournamentFilter = true;

    @Column(nullable = false)
    private boolean enablePlayerSearch = true;

    @Column(nullable = false)
    private boolean experimentalRegistration = false;
} 