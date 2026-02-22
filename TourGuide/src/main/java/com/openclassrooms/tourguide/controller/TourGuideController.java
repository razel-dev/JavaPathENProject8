package com.openclassrooms.tourguide.controller;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import gpsUtil.location.Location;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import tripPricer.Provider;

import com.openclassrooms.tourguide.service.RewardsService;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;
	
    @Autowired
    RewardsService rewardsService;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public VisitedLocation getLocation(@RequestParam String userName) {
    	return tourGuideService.getUserLocation(getUser(userName));
    }
    
       @RequestMapping("/getNearbyAttractions")
    public List<Map<String, Object>> getNearbyAttractions(@RequestParam String userName) {
        User user = getUser(userName);
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
        Location userLoc = visitedLocation.location;

        return rewardsService.getClosestAttractions(userLoc, 5).stream()
                .map(attraction -> {
                    Map<String, Object> attractionMap = new LinkedHashMap<>();
                    attractionMap.put("attractionName", attraction.attractionName);
                    attractionMap.put("attractionLatitude", attraction.latitude);
                    attractionMap.put("attractionLongitude", attraction.longitude);
                    attractionMap.put("userLatitude", userLoc.latitude);
                    attractionMap.put("userLongitude", userLoc.longitude);
                    attractionMap.put("distanceMiles", rewardsService.getDistance(attraction, userLoc));
                    attractionMap.put("rewardPoints", rewardsService.getRewardPoints(attraction, user));
                    return attractionMap;
                })
                .toList();
    }
    
    @RequestMapping("/getRewards") 
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(getUser(userName));
    }
       
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(getUser(userName));
    }
    
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   
}