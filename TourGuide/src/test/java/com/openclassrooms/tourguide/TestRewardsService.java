package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import com.openclassrooms.tourguide.tracker.Tracker;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;


@SpringBootTest
@ActiveProfiles("test")

public class TestRewardsService {

    @Autowired
    private GpsUtil gpsUtil;

    @Autowired
    private RewardsService rewardsService;

    @Autowired
    private TourGuideService tourGuideService;

    @Autowired
    private Tracker tracker;

    @BeforeEach
    void resetState() {
        // Réinitialise les utilisateurs en mémoire entre chaque test
        tourGuideService.clearAllUsers();
        // Remet le rayon de proximité à la valeur nominale attendue par les tests
        rewardsService.setProximityBuffer(10);
    }

    @Test
    public void userGetRewards() {
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtil.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        tourGuideService.trackUserLocation(user);
        List<UserReward> userRewards = user.getUserRewards();
        assertEquals(1, userRewards.size());
    }

	@Test
	public void isWithinAttractionProximity() {
		Attraction attraction = gpsUtil.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}

	@Test
	public void nearAllAttractions() {
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

        User user = new User(UUID.randomUUID(), "internalUser0", "000", "internalUser0@tourGuide.com");
        tourGuideService.addUser(user);

        Attraction anyAttraction = gpsUtil.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), anyAttraction, new Date()));

        rewardsService.calculateRewards(user);

        List<UserReward> userRewards = tourGuideService.getUserRewards(user);
        assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
	}

  
}
