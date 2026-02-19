package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

// Ici on étend Thread, MAIS on va aussi lancer ce même objet via un ExecutorService plus bas.
// En clair: deux manières de gérer le thread en même temps -> ça embrouille.
// Plus simple: ne pas étendre Thread, juste implémenter Runnable et laisser l’exécuteur gérer.
public class Tracker extends Thread {

    private Logger logger = LoggerFactory.getLogger(Tracker.class);

    // Intervalle d’attente (en secondes) entre deux cycles (on code ce que fait nativement le scheduler).

    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);

    // Un pool avec 1 thread pour “faire tourner” le tracker.
    // Problème: on soumet ci-dessous “this” qui est déjà un Thread -> double emploi inutile.
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final TourGuideService tourGuideService;

    
    // Problème: Java fournit déjà l’interruption de thread; du coup on a 2 mécanismes à gérer (ce flag + interrupt).
    private boolean stop = false;

    public Tracker(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;

        // On lance le tracker via l’exécuteur (executorService.submit(this)),
        // mais “this” est un Thread: mélange des genres -> ça complique sans bénéfice.
        executorService.submit(this);
    }

    /**
     * Assures to shut down the Tracker thread
     */
    public void stopTracking() {

        stop = true;
        executorService.shutdownNow();
    }

    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();
        // Boucle infinie “à la main” + sleep pour faire du périodique.
        // Ça marche, mais c’est fragile et verbeux (il existe un planificateur qui fait ça tout seul).
        while (true) {
            // Deux façons de s’arrêter: interruption OU flag “stop”.
            // Avoir deux chemins d’arrêt = plus de chances d’oublis/bugs.
            if (Thread.currentThread().isInterrupted() || stop) {
                logger.debug("Tracker stopping");
                break;
            }

            // On récupère tous les utilisateurs puis on les “trace” un par un.
            // Si la liste grossit, ça peut devenir lent;
            List<User> users = tourGuideService.getAllUsers();

            logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
            
            stopWatch.start();

            // Traitement un par un. Ok pour peu d’utilisateurs,
            // mais pas optimal si la charge augmente.
            users.forEach(u -> tourGuideService.trackUserLocation(u));

            stopWatch.stop();


            logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");

            // On remet le chrono à zéro pour la prochaine boucle.

            stopWatch.reset();

            try {

                // Problème: on gère à la main l’attente et les interruptions.
                // Un planificateur (ScheduledExecutorService) fait ça automatiquement et plus proprement.
                logger.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(trackingPollingInterval);
            } catch (InterruptedException e) {

                break;
            }
        }

    }
}
