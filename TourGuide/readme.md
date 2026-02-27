# Technologies

> Java 17  
> Spring Boot 3.X  
> JUnit 5  

# How to have gpsUtil, rewardCentral and tripPricer dependencies available ?

> Run : 
- mvn install:install-file -Dfile=/libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar  
- mvn install:install-file -Dfile=/libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar  
- mvn install:install-file -Dfile=/libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar

- Exécuter un script:
    - Dans PowerShell: ./scripts/start.ps1

- Si PowerShell bloque l’exécution:
    - Set-ExecutionPolicy -Scope CurrentUser RemoteSigned

- Accès rapides après start:
    - TourGuide: [http://localhost:8080](http://localhost:8080)
    - Actuator: [http://localhost:8081/actuator/prometheus](http://localhost:8081/actuator/prometheus)
    - Prometheus: [http://localhost:9090](http://localhost:9090)
    - Grafana: [http://localhost:3000](http://localhost:3000)

