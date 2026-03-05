package com.openclassrooms.tourguide.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import gpsUtil.location.VisitedLocation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import tripPricer.Provider;


/**
 * Représente un utilisateur de l'application TourGuide.
 * Cette classe stocke l'identité, les coordonnées de contact, l'horodatage
 * de la dernière localisation, l'historique des lieux visités, les récompenses
 * associées aux attractions, les préférences utilisateurs et les offres de voyage.

 * Remarque : les listes exposées par les getters sont modifiables et reflètent l'état interne.
 * Toute modification via ces listes modifiera directement l'objet {@code User}.

 */

@Getter
public class User {


    // Identité
    private final UUID userId;

    private final String userName;


    // Coordonnées
    @Setter
    private String phoneNumber;
    /**
     * -- GETTER --
     *  Retourne l'adresse e-mail.
     * -- SETTER --
     *  Met à jour l'adresse e-mail.
     *
     */
    @Setter
    private String emailAddress;

    /**
     * -- GETTER --
     *  Retourne l'horodatage de la dernière localisation.
     * -- SETTER --
     *  Définit l'horodatage de la dernière localisation.

     */
    // Localisation
    @Setter
    private Date latestLocationTimestamp;

    /**
     * -- GETTER --
     *  Retourne la liste des visites enregistrées.
     *  <p>La liste retournée est modifiable.</p>
         */
    // Données métier
    private List<VisitedLocation> visitedLocations = new ArrayList<>();
    /**
     * -- GETTER --
     *  Retourne la liste des récompenses.
     *  <p>La liste retournée est modifiable.</p>

     */
    private List<UserReward> userRewards = new ArrayList<>();
    /**
     * -- GETTER --
     *  Retourne les préférences de l'utilisateur.

     * -- SETTER --
     *  Définit les préférences de l'utilisateur.

     */
    @Setter
    private UserPreferences userPreferences = new UserPreferences();
    /**
     * -- GETTER --
     *  Retourne la liste des offres de voyage.
     *  <p>La liste retournée est modifiable.</p>
     *
     *
     * -- SETTER --
     *  Définit la liste des offres de voyage.

     */
    @Setter
    private List<Provider> tripDeals = new ArrayList<>();

    /**
     * Crée un utilisateur avec son identifiant, son nom et ses coordonnées.
     *
     * @param userId       identifiant unique de l'utilisateur
     * @param userName     nom d'utilisateur
     * @param phoneNumber  numéro de téléphone
     * @param emailAddress adresse e-mail
     */
    public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }
    
    // --- Identité ---

    // --- Coordonnées ---

    // --- Localisation ---

    // --- Historique des visites ---

    /**
     * Ajoute une visite à l'historique.
     *
     * @param visitedLocation la visite à ajouter
     */
    public void addToVisitedLocations(VisitedLocation visitedLocation) {
        visitedLocations.add(visitedLocation);
    }

    /**
     * Vide l'historique des visites.
     */
    public void clearVisitedLocations() {
        visitedLocations.clear();
    }
    
    /**
     * Retourne la dernière visite de l'historique.
     *
     * @return la dernière {@link VisitedLocation}
     * @throws IndexOutOfBoundsException si aucune visite n'est enregistrée
     */
    public VisitedLocation getLastVisitedLocation() {
        return visitedLocations.get(visitedLocations.size() - 1);
    }
    
    // --- Récompenses ---

    /**
     * Ajoute une récompense utilisateur si aucune récompense n'existe déjà
     * pour la même attraction (comparaison par nom d'attraction).
     *
     * @param userReward la récompense à ajouter
     */
    public void addUserReward(UserReward userReward) {
        if (userRewards.stream()
                .noneMatch(r -> r.attraction.attractionName.equals(userReward.attraction.attractionName))) {
            userRewards.add(userReward);
        }
    }

    // --- Préférences ---

    // --- Offres de voyage ---

}
