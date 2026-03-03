package com.openclassrooms.tourguide.service;

import java.util.*;

// import lombok.Setter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
// Ajout pour l'exécution asynchrone
import java.util.concurrent.*;

@Service
public class RewardsService {
    // Facteur de conversion milles nautiques -> milles terrestres
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // Rayon de proximité par défaut (en miles) pour l’attribution des récompenses
    private static final int DEFAULT_PROXIMITY_BUFFER = 10;

    // Rayon générique de proximité d'une attraction (en miles) pour la méthode de vérification dédiée
    private static final int ATTRACTION_PROXIMITY_RANGE = 200;

    // Ajuste dynamiquement le rayon de proximité utilisé pour attribuer les récompenses
    // proximity in miles (modifiable dynamiquement via les setters)
    @Setter
    private int proximityBuffer = DEFAULT_PROXIMITY_BUFFER;

    // Dépendances externes (GPS et calculateur de points)
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;
    @Autowired
    private RewardPointsService rewardPointsService;

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
        // rewardPointsService injecté par Spring via @Autowired
    }

    public void calculateRewards(User user) {
        // Copies pour éviter ConcurrentModificationException
        List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());
        // Utilise la liste d’attractions directement depuis gpsUtil
        List<Attraction> attractions = new ArrayList<>(gpsUtil.getAttractions());

        Set<String> rewardedAttractionNames = new HashSet<>();
        user.getUserRewards().forEach(r -> rewardedAttractionNames.add(r.attraction.attractionName));

        // Inversion: on boucle d'abord sur les attractions, puis sur les visites
        for (Attraction attraction : attractions) {
            if (rewardedAttractionNames.contains(attraction.attractionName)) {
                continue; // déjà récompensée par le fast path ou précédemment
            }
            for (VisitedLocation visitedLocation : userLocations) {
                if (isNearAttraction(visitedLocation, attraction)) {
                    user.addUserReward(new UserReward(
                            visitedLocation,
                            attraction,
                            getRewardPoints(attraction, user)
                    ));
                    rewardedAttractionNames.add(attraction.attractionName);
                    break; // passe à l’attraction suivante
                }
            }
        }
    }

    // Indique si une localisation est dans la zone de proximité générique de l'attraction
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) <= ATTRACTION_PROXIMITY_RANGE;
    }

    public List<Attraction> getClosestAttractions(Location from, int limit) {
        // Tri lisible via Comparator.comparingDouble
        return gpsUtil.getAttractions().stream()
                .sorted(Comparator.comparingDouble(a -> getDistance(a, from)))
                .limit(limit)
                .toList();
    }

    // Teste si la visite est suffisamment proche de l'attraction (utilise proximityBuffer courant)
    private boolean isNearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) <= proximityBuffer;
    }

    // Délégation vers le service de points (cache), avec fallback RewardCentral si non injecté
    public int getRewardPoints(Attraction attraction, User user) {
        if (rewardPointsService != null) {
            return rewardPointsService.getRewardPoints(attraction, user);
        }
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    // Calcule la distance sphérique (grand cercle) entre deux localisations
    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }
}
