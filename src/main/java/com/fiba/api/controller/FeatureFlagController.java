package com.fiba.api.controller;

import com.fiba.api.service.FeatureFlagService;
import com.fiba.api.model.FeatureFlags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    @GetMapping
    public ResponseEntity<FeatureFlags> getFeatureFlags() {
        log.info("Получение настроек feature flags");
        return ResponseEntity.ok(featureFlagService.getFeatureFlags());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeatureFlags> updateFeatureFlags(@RequestBody FeatureFlags flags) {
        log.info("Обновление feature flags: {}", flags);
        return ResponseEntity.ok(featureFlagService.updateFeatureFlags(flags));
    }
} 