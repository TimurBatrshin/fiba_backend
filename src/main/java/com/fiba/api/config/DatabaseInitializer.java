package com.fiba.api.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        // Update tournaments with null names
        int updatedRows = entityManager.createNativeQuery(
            "UPDATE tournaments SET name = 'Unnamed Tournament' WHERE name IS NULL")
            .executeUpdate();
        
        System.out.println("Updated " + updatedRows + " tournament records with default name");
    }
} 