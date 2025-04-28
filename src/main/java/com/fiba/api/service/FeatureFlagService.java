package com.fiba.api.service;

import com.fiba.api.model.FeatureFlags;
import com.fiba.api.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;
    private static final String DEFAULT_ID = "default";

    @Transactional(readOnly = true)
    public FeatureFlags getFeatureFlags() {
        return featureFlagRepository.findById(DEFAULT_ID)
            .orElseGet(() -> {
                FeatureFlags defaultFlags = new FeatureFlags();
                defaultFlags.setId(DEFAULT_ID);
                return featureFlagRepository.save(defaultFlags);
            });
    }

    @Transactional
    public FeatureFlags updateFeatureFlags(FeatureFlags flags) {
        FeatureFlags existing = getFeatureFlags();
        
        // Обновляем только переданные флаги
        if (flags.isDisableBackend() != existing.isDisableBackend()) {
            existing.setDisableBackend(flags.isDisableBackend());
        }
        if (flags.isShowTopPlayers() != existing.isShowTopPlayers()) {
            existing.setShowTopPlayers(flags.isShowTopPlayers());
        }
        if (flags.isShowAdminPanel() != existing.isShowAdminPanel()) {
            existing.setShowAdminPanel(flags.isShowAdminPanel());
        }
        if (flags.isEnableAdminPage() != existing.isEnableAdminPage()) {
            existing.setEnableAdminPage(flags.isEnableAdminPage());
        }
        if (flags.isEnableTournamentFilter() != existing.isEnableTournamentFilter()) {
            existing.setEnableTournamentFilter(flags.isEnableTournamentFilter());
        }
        if (flags.isEnablePlayerSearch() != existing.isEnablePlayerSearch()) {
            existing.setEnablePlayerSearch(flags.isEnablePlayerSearch());
        }
        if (flags.isExperimentalRegistration() != existing.isExperimentalRegistration()) {
            existing.setExperimentalRegistration(flags.isExperimentalRegistration());
        }

        return featureFlagRepository.save(existing);
    }
} 