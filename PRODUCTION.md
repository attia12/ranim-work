# Checklist de mise en production — Campway AI Recommendation

## Secrets & Configuration

- [ ] Tous les secrets du fichier `.env.example` remplacés par de vraies valeurs (jamais commités)
- [ ] `JWT_SECRET` : au moins 64 caractères aléatoires (ex: `openssl rand -hex 32`)
- [ ] `DB_PASSWORD` / `MYSQL_ROOT_PASSWORD` : mots de passe forts, non réutilisés
- [ ] `STRIPE_SECRET_KEY` / `STRIPE_PUBLISHABLE_KEY` : clés de production (pas `sk_test_`)
- [ ] `REDIS_HOST` : pointé vers Redis sécurisé (mot de passe si exposé en réseau)

## Sécurité réseau

- [ ] Le service FastAPI (`campsite-recommender:8001`) n'est PAS exposé publiquement — uniquement accessible via le réseau Docker interne `campway-net`
- [ ] Nginx bloque toute route `/ai/` en externe (voir `nginx/campsite.conf`)
- [ ] HTTPS configuré (Let's Encrypt ou certificat géré par le load balancer)
- [ ] Headers de sécurité Nginx vérifiés (X-Frame-Options, CSP, etc.)

## Redis

- [ ] Mot de passe Redis configuré : `requirepass <mot_de_passe_fort>` dans `redis.conf`
- [ ] Redis non exposé sur le réseau public (port 6379 uniquement en interne)
- [ ] TTL par défaut 30 minutes — à affiner après 2 semaines de données d'usage

## Rate limiting

- [ ] Ajouter rate limiting sur `GET /api/v1/campsites/recommended` : max 10 appels/min/utilisateur
  - Option A : Bucket4j (Spring Boot)
  - Option B : Nginx `limit_req_zone` par IP

## Monitoring & Observabilité

- [ ] `GET /actuator/health` (Spring Boot) vérifié — mettre derrière auth ou IP whitelist en prod
- [ ] `GET /health` (FastAPI) vérifié — inaccessible depuis l'extérieur (réseau interne seulement)
- [ ] Logs JSON structurés des deux services agrégés (ex: Loki + Grafana, ou ELK)
- [ ] Alertes configurées sur : 503 de Spring Boot → AI service, latence > 8s, taux d'erreur > 1%

## Open-Meteo (API météo gratuite)

- [ ] Limite : 10 000 appels/jour en tier gratuit
- [ ] Avec cache 30 min par utilisateur et 5 campsites/appel :
  - Appels journaliers ≈ `utilisateurs_actifs × 5 × (1440 / 30)` = `utilisateurs × 240`
  - Seuil sûr : ~41 utilisateurs actifs/jour sur le tier gratuit
- [ ] Si trafic > 50 utilisateurs/jour : envisager cache par campsite (pas par utilisateur) et TTL 1h
- [ ] Si trafic > 200 utilisateurs/jour : souscrire au tier commercial Open-Meteo

## Comportement de repli (fallback)

- [ ] FastAPI en panne → Spring Boot retourne HTTP 503 avec `{"error":"recommendation_unavailable","fallback":true}`
- [ ] Angular : `RecommendationService` retourne `EMPTY` sur erreur → le carousel affiche "Explorez nos campsites"
- [ ] Redis en panne → `@Cacheable` échoue silencieusement (selon config) → appel AI à chaque requête
- [ ] Open-Meteo timeout → score météo = 0.5 (neutre), pas d'exception propagée

## Performance

- [ ] FastAPI : `--workers 2` en production (voir Dockerfile CMD)
- [ ] Spring Boot : `@Cacheable("recommendations")` avec TTL 30 min par userId
- [ ] Cold start FastAPI : ~500ms (httpx.AsyncClient initialization) — première requête lente, les suivantes < 200ms
- [ ] Vérifier que `findRecentConfirmedByCamperId` est indexé sur `camper_id + check_in_date + status`

## Avant la mise en ligne

- [ ] `docker compose up --build` testé localement avec toutes les variables `.env` de production
- [ ] Endpoint `POST /api/v1/campsites/recommended` (FastAPI) testé avec curl depuis le conteneur backend
- [ ] Cache Redis vérifié : `redis-cli KEYS "recommendations::*"` après quelques requêtes
- [ ] Tests CI passent sur la branche master avant tout déploiement
