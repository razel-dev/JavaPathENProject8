package com.openclassrooms.tourguide.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;

/**
 * Représente un utilisateur de l'application TourGuide.
 * Cette classe stocke l'identité, les coordonnées de contact, l'horodatage
 * de la dernière localisation, l'historique des lieux visités, les récompenses
 * associées aux attractions, les préférences utilisateurs et les offres de voyage.

 * Remarque : les listes exposées par les getters sont modifiables et reflètent l'état interne.
 * Toute modification via ces listes modifiera directement l'objet {@code User}.

 */
public class User {

    // Identité
    private final UUID userId;
    private final String userName;

    // Coordonnées
    private String phoneNumber;
    private String emailAddress;

    // Localisation
    private Date latestLocationTimestamp;

    // Données métier
    private List<VisitedLocation> visitedLocations = new ArrayList<>();
    private List<UserReward> userRewards = new ArrayList<>();
    private UserPreferences userPreferences = new UserPreferences();
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

    /**
     * Retourne l'identifiant unique de l'utilisateur.
     *
     * @return l'UUID de l'utilisateur
     */
    public UUID getUserId() {
        return userId;
    }
    
    /**
     * Retourne le nom de l'utilisateur.
     *
     * @return le nom d'utilisateur
     */
    public String getUserName() {
        return userName;
    }
    
    // --- Coordonnées ---

    /**
     * Met à jour le numéro de téléphone.
     *
     * @param phoneNumber le nouveau numéro de téléphone
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    /**
     * Retourne le numéro de téléphone.
     *
     * @return le numéro de téléphone
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Met à jour l'adresse e-mail.
     *
     * @param emailAddress la nouvelle adresse e-mail
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    /**
     * Retourne l'adresse e-mail.
     *
     * @return l'adresse e-mail
     */
    public String getEmailAddress() {
        return emailAddress;
    }
    
    // --- Localisation ---

    /**
     * Définit l'horodatage de la dernière localisation.
     *
     * @param latestLocationTimestamp la date/heure de la dernière localisation
     */
    public void setLatestLocationTimestamp(Date latestLocationTimestamp) {
        this.latestLocationTimestamp = latestLocationTimestamp;
    }
    
    /**
     * Retourne l'horodatage de la dernière localisation.
     *
     * @return la date/heure de la dernière localisation
     */
    public Date getLatestLocationTimestamp() {
        return latestLocationTimestamp;
    }
    
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
     * Retourne la liste des visites enregistrées.
     * <p>La liste retournée est modifiable.</p>
     *
     * @return la liste des {@link VisitedLocation}
     */
    public List<VisitedLocation> getVisitedLocations() {
        return visitedLocations;
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
    
    /**
     * Retourne la liste des récompenses.
     * <p>La liste retournée est modifiable.</p>
     *
     * @return la liste des {@link UserReward}
     */
    public List<UserReward> getUserRewards() {
        return userRewards;
    }
    
    // --- Préférences ---

    /**
     * Retourne les préférences de l'utilisateur.
     *
     * @return l'objet {@link UserPreferences} courant
     */
    public UserPreferences getUserPreferences() {
        return userPreferences;
    }
    
    /**
     * Définit les préférences de l'utilisateur.
     *
     * @param userPreferences les nouvelles préférences
     */
    public void setUserPreferences(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }
    
    // --- Offres de voyage ---

    /**
     * Définit la liste des offres de voyage.
     *
     * @param tripDeals la liste de fournisseurs/offres
     */
    public void setTripDeals(List<Provider> tripDeals) {
        this.tripDeals = tripDeals;
    }
    
    /**
     * Retourne la liste des offres de voyage.
     * <p>La liste retournée est modifiable.</p>
     *
     * @return la liste des {@link Provider}
     */
    public List<Provider> getTripDeals() {
        return tripDeals;
    }

}
