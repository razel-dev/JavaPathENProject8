package com.openclassrooms.tourguide.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import gpsUtil.location.Attraction;
import com.openclassrooms.tourguide.user.User;

@Service
public class RewardPointsService {

    private final RewardCentral rewardCentral;

    public RewardPointsService(RewardCentral rewardCentral) {
        this.rewardCentral = rewardCentral;
    }

    // Mise en cache centralisée des points de récompense
    @Cacheable(
        cacheNames = "rewardPoints",
        key = "#attraction.attractionId.toString() + '-' + #user.userId.toString()",
        sync = true,
        cacheManager = "cacheManager"
    )
    public int getRewardPoints(Attraction attraction, User user) {
        return rewardCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }
}