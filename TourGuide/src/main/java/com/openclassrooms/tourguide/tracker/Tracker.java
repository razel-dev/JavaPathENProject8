package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Slf4j
@RequiredArgsConstructor
@Component
public class Tracker implements Runnable {

    private static final long TRACKING_POLLING_INTERVAL_SECONDS = TimeUnit.MINUTES.toSeconds(5);

    // Scheduler pour la périodicité (1 thread suffit)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Pool multi-threads pour paralléliser les utilisateurs pendant un cycle
    private final ExecutorService workerPool = Executors.newFixedThreadPool(
            Math.max(1, Runtime.getRuntime().availableProcessors()),
            r -> {
                Thread t = new Thread(r);
                t.setName("tracker-worker-" + t.getId());
                t.setDaemon(true);
                return t;
            });

    private final TourGuideService tourGuideService;

    @PostConstruct
    public void start() {
        scheduler.scheduleWithFixedDelay(
                this,
                0,
                TRACKING_POLLING_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
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
            // Soumission en parallèle, attente de fin du cycle
            CompletableFuture<?>[] futures = users.stream()
                    .map(u -> CompletableFuture.runAsync(() -> tourGuideService.trackUserLocation(u), workerPool))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();
        } finally {
            stopWatch.stop();
            log.debug("Temps écoulé du cycle du Tracker: {} secondes.",
                    TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
        }
    }

    @PreDestroy
    public void stopTracking() {
        log.debug("Arrêt du Tracker");
        scheduler.shutdownNow();
        workerPool.shutdownNow();
    }
}
