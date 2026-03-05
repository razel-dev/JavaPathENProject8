package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

// import com.openclassrooms.tourguide.helper.InternalTestHelper; // SUPPRIMÉ: import inutile

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.tracker.Tracker; // AJOUT

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

    @Autowired
    private Tracker tracker; // AJOUT

    @Test
    public void highVolumeTrackLocation() {
        // on ne modifie plus ici le nombre d'utilisateurs.
        // Spring lit InternalTestHelper au démarrage du contexte (avant les tests).
        // InternalTestHelper.setInternalUserNumber(100);

        // Récupération de tous les utilisateurs internes initialisés par le service
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Exécute un cycle complet du Tracker
        tracker.run(); // REMPLACE tourGuideService.tracker.run()

        // Arrêt du chrono (plus besoin d'arrêter manuellement le tracker: @PreDestroy s’en charge à la fin du contexte)
        stopWatch.stop();

        System.out.println("highVolumeTrackLocation: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    @Test
    public void highVolumeGetRewards() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Attraction attraction = gpsUtil.getAttractions().get(0);
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        int parallelism = Math.max(1, Runtime.getRuntime().availableProcessors()*4);
        System.out.println("Parallelism choisi (rewards) = " + parallelism);
        tourGuideService.calculateAllRewardsInParallel(parallelism);
        System.out.println("CPUs visibles JVM (rewards) = " + Runtime.getRuntime().availableProcessors());

        for (User user : allUsers) {
            assertFalse(user.getUserRewards().isEmpty());
        }

        stopWatch.stop();
        // SUPPRIMÉ: tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeGetRewards: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }
}