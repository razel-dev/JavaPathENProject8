package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;
import tripPricer.TripPricer;
import lombok.extern.slf4j.Slf4j;
// Ajout pour le parallélisme contrôlé


@Service
@Slf4j
public class TourGuideService {

    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    boolean testMode = true;


    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;

        Locale.setDefault(Locale.US);

        if (testMode) {
            log.info("Mode de test activé");

            log.debug("Initialisation des utilisateurs de test");
            initializeInternalUsers();
            log.debug("Fin de l’initialisation des utilisateurs de test");

        } else {

        }
        tracker = new Tracker(this);
        log.debug("Démarrage du Tracker");

        if (!testMode) {
            tracker.start();
        }

        addShutDownHook();

    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        return user.getVisitedLocations().isEmpty()
                ? trackUserLocation(user)
                : user.getLastVisitedLocation();
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(internalUserMap.values());
    }

    // Exécute le calcul des récompenses pour tous les utilisateurs en parallèle
    // 'parallelism' contrôle le nombre maximum de threads utilisés.
    public void calculateAllRewardsInParallel(int parallelism) {
        List<User> users = getAllUsers();

        // Remplacement de parallelStream/ForkJoinPool: on utilise un ExecutorService dédié
        // pour garantir l'utilisation de 'parallelism' threads (le commonPool n'est plus impliqué).
       ExecutorService es = Executors.newFixedThreadPool(Math.max(1, parallelism));
        try {
           CompletableFuture.allOf(
                    users.stream()
                            .map(u -> CompletableFuture.runAsync(() -> rewardsService.calculateRewards(u), es))
                            .toArray(CompletableFuture[]::new)
            ).join();
        } finally {
            es.shutdown();
        }
    }


    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    public List<Provider> getTripDeals(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
        List<Provider> providers = tripPricer.getPrice(TRIP_PRICER_API_KEY, user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
        // Découplage du rayon: renvoie toujours les 5 plus proches par distance
        return rewardsService.getClosestAttractions(visitedLocation.location, 5);
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> tracker.stopTracking()));
    }

    /**********************************************************************************
     *
     * Méthodes ci-dessous : pour les tests internes
     *
     **********************************************************************************/
    private static final String TRIP_PRICER_API_KEY = "test-server-api-key";
    private static final double LONGITUDE_MIN = -180.0;
    private static final double LONGITUDE_MAX = 180.0;
    private static final double LATITUDE_MIN = -85.05112878;
    private static final double LATITUDE_MAX = 85.05112878;
    private final Random random = new Random();

    // Pour les tests, les utilisateurs internes sont stockés en mémoire
    private final ConcurrentMap<String, User> internalUserMap = new ConcurrentHashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        log.debug("Création de {} utilisateurs de test internes.", InternalTestHelper.getInternalUserNumber());
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
                    new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        return LONGITUDE_MIN + random.nextDouble() * (LONGITUDE_MAX - LONGITUDE_MIN);
    }

    private double generateRandomLatitude() {
        return LATITUDE_MIN + random.nextDouble() * (LATITUDE_MAX - LATITUDE_MIN);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(random.nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
