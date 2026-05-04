# campsite-recommender

Moteur de recommandation déterministe pour Campway. Service FastAPI autonome, appelé par le backend Spring Boot via HTTP interne.

## Architecture

Le service reçoit du backend Spring Boot : l'historique de réservations de l'utilisateur, ses avis, et la liste de tous les campsites disponibles. Il calcule un score pondéré pour chaque campsite candidat et retourne les 5 meilleurs. Aucune base de données n'est utilisée : toutes les données arrivent dans le corps de la requête POST.

Trois couches internes : (1) `profile_builder` analyse l'historique pour construire le profil utilisateur (préférences terrain, budget moyen, saison préférée, note moyenne donnée) ; (2) `scoring_engine` applique la formule pondérée ; (3) `weather_service` enrichit chaque campsite candidat avec la météo actuelle via Open-Meteo (API gratuite, sans clé).

## Démarrage local

```bash
cd campsite-recommender
cp .env.example .env
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
```

Tester l'endpoint :
```bash
curl -s -X POST http://localhost:8001/api/v1/campsites/recommended \
  -H "Content-Type: application/json" \
  -d '{"user_id":1,"bookings":[],"reviews":[],"all_campsites":[{"id":1,"name":"Camp Test","natural_features":["FOREST"],"price_per_night":45.0,"latitude":48.85,"longitude":2.35}]}' | python -m json.tool
```

Lancer les tests :
```bash
pytest -v --tb=short
```

## Formule de scoring

| Composante | Poids | Description |
|------------|-------|-------------|
| Terrain    | 35%   | Adéquation entre les features du campsite et les préférences de l'utilisateur |
| Budget     | 25%   | Proximité du prix du campsite avec le budget moyen de l'utilisateur |
| Saison     | 20%   | Mois courant vs saison préférée de l'utilisateur |
| Note       | 10%   | Note donnée par l'utilisateur ou note globale du campsite |
| Météo      | 10%   | Score météo actuel via Open-Meteo (code WMO) |

Score final = somme pondérée × 100, entre 0 et 100.

## Ajuster les poids

Modifier le dictionnaire `WEIGHTS` dans `app/services/scoring_engine.py` :

```python
class ScoringEngine:
    WEIGHTS = {
        "terrain": 0.35,  # augmenter pour favoriser l'adéquation terrain
        "budget":  0.25,  # augmenter pour favoriser les budgets proches
        "season":  0.20,
        "rating":  0.10,
        "weather": 0.10,
    }
```

Les poids doivent sommer à 1.0. Aucun redéploiement nécessaire si le service est rechargé (--reload en dev).

## Dépannage

**Redis connection refusée (côté Spring Boot)** : vérifier que Redis est démarré (`docker compose up redis`). Le service FastAPI n'utilise pas Redis directement.

**Open-Meteo timeout** : le service retourne `score=0.5, description="Inconnu"` pour tout campsite dont la météo n'a pas pu être récupérée. Aucune exception n'est propagée.

**Démarrage lent (cold start)** : avec `--workers 2`, la première requête prend ~500ms le temps de créer le `httpx.AsyncClient`. Les suivantes sont quasi-instantanées.

**`natural_features` vide pour tous les campsites** : s'assurer que le champ est renseigné côté Spring Boot (colonne `natural_features` dans la table `campsites`). Si vide, le score terrain vaut 0.3 (neutre) pour tous les candidats.
