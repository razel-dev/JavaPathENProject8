package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Implémente Runnable : le scheduler gère la planification périodique.
@Slf4j
@RequiredArgsConstructor
public class Tracker implements Runnable {

    // Intervalle entre deux cycles (en secondes), géré par le scheduler.
    private static final long TRACKING_POLLING_INTERVAL_SECONDS = TimeUnit.MINUTES.toSeconds(5);
    // Planificateur à un seul thread pour l’exécution périodique.
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final TourGuideService tourGuideService;


    public void start() {
        scheduler.scheduleWithFixedDelay(
                this,
                0,
                TRACKING_POLLING_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    /**
     * Arrête proprement le scheduler du Tracker.
     */
    public void stopTracking() {
        log.debug("Arrêt du Tracker");
        scheduler.shutdownNow();
    }

    @Override
    public void run() {
        if (Thread.currentThread().isInterrupted()) {
            log.debug("Tracker interrompu avant d’exécuter le cycle");
            return;
        }

        List<User> users = tourGuideService.getAllUsers();
        log.debug("Démarrage d’un cycle du Tracker. Suivi de {} utilisateurs.", users.size());

        StopWatch stopWatch = StopWatch.createStarted();
        try {
            users.forEach(tourGuideService::trackUserLocation);
        } finally {
            stopWatch.stop();
            log.debug("Temps écoulé du cycle du Tracker: {} secondes.",
                    TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
        }
    }
}
