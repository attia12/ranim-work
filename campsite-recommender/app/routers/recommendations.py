from datetime import date

from fastapi import APIRouter, Depends

from app.dependencies import get_weather_service, get_scoring_engine
from app.models.recommendation import RecommendationRequest, RecommendedCampsite
from app.services.scoring_engine import ScoringEngine
from app.services.weather_service import WeatherService

router = APIRouter(tags=["recommendations"])


@router.post("/recommended", response_model=list[RecommendedCampsite])
async def get_recommendations(
    body: RecommendationRequest,
    weather_svc: WeatherService = Depends(get_weather_service),
    engine: ScoringEngine = Depends(get_scoring_engine),
) -> list[RecommendedCampsite]:
    profile = engine.build_user_profile(body.bookings, body.reviews)
    reviews_map: dict[int, float] = {r.campsite_id: r.score for r in body.reviews}

    campsite_coords = [
        (c.id, c.latitude, c.longitude) for c in body.all_campsites
    ]
    weather_scores = await weather_svc.get_weather_for_campsites(campsite_coords)

    current_month = date.today().month
    ranked = engine.rank_campsites(
        body.all_campsites, profile, weather_scores, current_month, reviews_map
    )

    results: list[RecommendedCampsite] = []
    for campsite, breakdown in ranked:
        weather = weather_scores.get(campsite.id)
        weather_desc = weather.description if weather is not None else "Inconnu"
        results.append(
            RecommendedCampsite(
                campsite_id=campsite.id,
                campsite_name=campsite.name,
                score=breakdown.total,
                score_breakdown=breakdown,
                weather_description=weather_desc,
                natural_features=campsite.natural_features,
                price_per_night=campsite.price_per_night,
                image_url=campsite.image_url,
            )
        )

    return results
