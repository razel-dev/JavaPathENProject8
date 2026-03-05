package com.openclassrooms.tourguide.config;

import com.openclassrooms.tourguide.service.TourGuideService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("test")
@RequiredArgsConstructor
public class TestDataInitializer {

    private final TourGuideService tourGuideService;

    @PostConstruct
    public void init() {
        log.info("Profil 'test' actif: initialisation des utilisateurs de test");
        tourGuideService.initializeInternalUsers();
        log.info("Initialisation des utilisateurs de test terminée");
    }
}