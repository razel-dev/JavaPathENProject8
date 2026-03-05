package com.openclassrooms.tourguide.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import gpsUtil.location.Attraction;
import com.openclassrooms.tourguide.user.User;

@RequiredArgsConstructor
@Service
public class RewardPointsService {

    private final RewardCentral rewardCentral;


    

    // Mise en cache centralisée des points de récompense
    // Note simple:
    // - Sans 'key', Spring crée une clé par défaut avec les 2 paramètres (SimpleKey(attraction, user)).
    // - Cela suppose que equals/hashCode soient bien définis sur Attraction et User.
    // - Comme Attraction vient d'une dépendance externe (pas modifiable), on recommande
    // Pourquoi mettre en cache ?
    // - Le calcul de distance est un petit calcul CPU local (trigonométrie), très rapide.
    // - L'appel à RewardCentral, lui, simule un appel externe lent (sleep aléatoire jusqu’à ~1 seconde),
    //   donc il bloque un thread, a une latence variable et coûte bien plus cher.
    // - Conséquence : on met en cache le résultat par duo (attractionId-userId) pour éviter
    //   de payer cette latence à chaque fois. Avec sync=true, un seul thread calcule une clé manquante
    //   pendant que les autres attendent le résultat (évite l’« effet troupeau »).


    @Cacheable(
        cacheNames = "rewardPoints",
        key = "#attraction.attractionId.toString() +'-'+#user.userId.toString()",
        sync = true,
        cacheManager = "cacheManager"
    )
    public int getRewardPoints(Attraction attraction, User user) {
        return rewardCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }
}