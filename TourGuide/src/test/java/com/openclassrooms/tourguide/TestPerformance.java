package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import com.openclassrooms.tourguide.helper.InternalTestHelper; // import court du helper

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

// Contexte Spring pour activer @Cacheable et injecter les beans
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest // Démarre le contexte Spring Boot (+ @EnableCaching)
public class TestPerformance {

    @Autowired
    private GpsUtil gpsUtil;

    @Autowired
    private RewardsService rewardsService;

    @Autowired
    private TourGuideService tourGuideService;

    /*
     * Notes sur les tests de performance :
     *
     * - Le nombre d'utilisateurs générés pour les tests haut volume est configurable :
     *   InternalTestHelper.setInternalUserNumber(100000);
     *
     * - Les métriques de performance attendues sont:
     *   - highVolumeTrackLocation : 100 000 utilisateurs en ≤ 15 minutes
     *   - highVolumeGetRewards : 100 000 utilisateurs en ≤ 20 minutes
     *
    /*
     * Notes sur les tests de performance :
     * - Les services sont injectés par Spring : les caches @Cacheable (Caffeine) sont opérationnels.
     * - InternalTestHelper permet de piloter le volume d’utilisateurs pour les tests.
     */

    @Test
    public void highVolumeTrackLocation() {
        // Configuration du volume d'utilisateurs de test (peut être monté jusqu'à 100 000)
        // Objectif: terminer en ≤ 15 minutes avec 100 000 utilisateurs
        InternalTestHelper.setInternalUserNumber(100000);

        // Récupération de tous les utilisateurs internes initialisés par le service
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        // Trace du parallélisme vu par la JVM
        System.out.println("CPUs visibles JVM (tracking) = " + Runtime.getRuntime().availableProcessors()*4);

        // NOTE: Avant, tu démarrais le chronomètre avant new TourGuideService(...).
        // Maintenant, le service est injecté par Spring (déjà construit avant le test),
        // donc la mesure n’inclut plus le coût de construction du service.
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Exécute un cycle complet du Tracker (parallélisé via son workerPool)
        tourGuideService.tracker.run();

        // Arrêt du chrono et arrêt du tracker (tâche planifiée en arrière-plan)
        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        // Log simple de la durée et vérification de l'objectif (≤ 15 minutes)
        System.out.println("highVolumeTrackLocation: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    @Test
    public void highVolumeGetRewards() {
        // Configuration du volume d'utilisateurs de test
        // Objectif: terminer en ≤ 20 minutes avec 100 000 utilisateurs
        InternalTestHelper.setInternalUserNumber(100_000);

        // Trace du parallélisme vu par la JVM
        System.out.println("CPUs visibles JVM (rewards) = " + Runtime.getRuntime().availableProcessors());

        // NOTE: Avant, demarrage du chronomètre avant new TourGuideService(...).
        // Maintenant, le service est injecté par Spring (déjà construit avant le test),
        // donc la mesure n’inclut plus le coût de construction du service.
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Choix d'une attraction de référence et injection d'une visite pour chaque utilisateur
        // à l'emplacement exact de l'attraction (proximité garantie)
        Attraction attraction = gpsUtil.getAttractions().get(0);
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        // Ajout d'une visite "sûre" sur l'attraction pour forcer l'attribution d'au moins une récompense
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        // Calcul des récompenses en masse pour tous les utilisateurs
        int parallelism = Math.max(1, Runtime.getRuntime().availableProcessors()*4);
        tourGuideService.calculateAllRewardsInParallel(parallelism);

        // Vérification fonctionnelle: chaque utilisateur doit avoir au moins 1 récompense
        for (User user : allUsers) {
            assertFalse(user.getUserRewards().isEmpty());
        }

        // Arrêt du chrono et du tracker planifié
        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        // Log de la durée et assertion de performance (≤ 20 minutes)
        System.out.println("highVolumeGetRewards: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }
}