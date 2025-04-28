package com.fiba.api.repository;

import com.fiba.api.model.FeatureFlags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlags, String> {
} 