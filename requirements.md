Idées pour les fonctionnalités du projet :

Nous developpons une application qui va gérer un batiment auto suffisant energétiquement.
L'idée serait de faire différentes fonctions qui n'ont pas toute la même utilitée afin d'utiliser les données différements.

Le batiment as (les fonctionnalités factultativent sont entre paranthèses):

- Une réserve d'eau. La réserve se remplit avec les précipitations (et se vide à l'utilisation);
- Des éoliennes. Elles sont activent qu'avec un minimum de vent à l'extérieur (et produise de l'énergie en fonction de la puissance du vent);
--> De même pour des potentiels panneaux solaires (on pourrait faire la somme de l'energie générée par tout ces systèmes);
- Des rideaux. Ils s'activent si la luminosité extérieur est au dessus d'un certains seuil;
- Des éclairages. Ils s'activent en fonction de l'heure de la journée ou de la luminosité actuelle s'il fait jour. (Ils consomment de l'énergie);
- Un système de chauffage. Maintiens la température à un certain niveau en fonction de la température extérieur. (Il consomme de l'énergie);
- Des plantations. Consomme de l'eau de la réserve;

Etape du développement :
- Get les données de l'api : Fait

    Production de ressources :
    - If IsDay == 1 : faire de l'electricité
        - production d'electricité par panneau solaire = 100 - couverture nuageuse
    - production d'électricité par éolienne = wind_speed_10m / 2
    - production d'eau = precipitation

    Consommation de ressources :
    - If IsDay == 0 || couverture nuageuse > 70 : consomme 5kWh
    - If temperature < 16 : consomme 10kWh + (res * 0.1)pour chaque degrés sous les 16 degrés
    - If precipitation == 0 : consomme 50ml

    Stockage des ressources :
    - stockage electricité (max 10 000 kWh) : start à 500kWh
    - stockage eau (max 200 L) : start à 20L
    - toutes les 30sec : ajouter ressources produites au stockages
    - toutes les 30sec : retirer ressources consommées au stockages