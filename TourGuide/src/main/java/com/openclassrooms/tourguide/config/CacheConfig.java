package com.openclassrooms.tourguide.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.DAYS) // TTL 24h
                        .maximumSize(200_000) // doublage des previsions journalieres et compatible avec ma RAM
                        .recordStats()
        );
        manager.setCacheNames(List.of("rewardPoints"));
        return manager;
    }
}