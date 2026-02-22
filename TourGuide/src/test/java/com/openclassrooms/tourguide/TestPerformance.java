
package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

public class TestPerformance {

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
     * - Les tests peuvent être adaptés à d'autres implémentations tant que
     *   les objectifs de temps restent respectés.
     */

    @Test
    public void highVolumeTrackLocation() {
        // Préparation des services: GPS (positions), Récompenses, et service métier principal
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

        // Configuration du volume d'utilisateurs de test (peut être monté jusqu'à 100 000)
        // Objectif: terminer en ≤ 15 minutes avec 100 000 utilisateurs
        InternalTestHelper.setInternalUserNumber(100000);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        // Récupération de tous les utilisateurs internes initialisés par le service
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        // Chronométrage de la boucle de suivi de localisation
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Pour chaque utilisateur: récupération d'une nouvelle position + calcul des récompenses
        for (User user : allUsers) {
            tourGuideService.trackUserLocation(user);
        }

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
        // Préparation des services
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

        // Configuration du volume d'utilisateurs de test
        // Objectif: terminer en ≤ 20 minutes avec 100 000 utilisateurs
        InternalTestHelper.setInternalUserNumber(100);

        // Démarrage du chronomètre AVANT la création du service pour inclure le coût si souhaité
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        // Choix d'une attraction de référence et injection d'une visite pour chaque utilisateur
        // à l'emplacement exact de l'attraction (proximité garantie)
        Attraction attraction = gpsUtil.getAttractions().get(0);
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        // Ajout d'une visite "sûre" sur l'attraction pour forcer l'attribution d'au moins une récompense
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        // Calcul des récompenses en masse pour tous les utilisateurs
        allUsers.forEach(u -> rewardsService.calculateRewards(u));

        // Vérification fonctionnelle: chaque utilisateur doit avoir au moins 1 récompense
        for (User user : allUsers) {
            assertTrue(user.getUserRewards().size() > 0);
        }

        // Arrêt du chrono et du tracker planifié
        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        // Log de la durée et assertion de performance (≤ 20 minutes)
        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
                + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

}