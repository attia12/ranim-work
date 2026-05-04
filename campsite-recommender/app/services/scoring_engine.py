from app.models.campsite import CampsiteDTO, NaturalFeature
from app.models.booking import BookingHistory
from app.models.review import ReviewHistory
from app.models.recommendation import ScoreBreakdown
from app.services.profile_builder import (
    UserProfile,
    build_user_profile,
    get_season_score,
)
from app.services.weather_service import WeatherResult


class ScoringEngine:
    WEIGHTS: dict[str, float] = {
        "terrain": 0.35,
        "budget": 0.25,
        "season": 0.20,
        "rating": 0.10,
        "weather": 0.10,
    }

    def build_user_profile(
        self, bookings: list[BookingHistory], reviews: list[ReviewHistory]
    ) -> UserProfile:
        return build_user_profile(bookings, reviews)

    def _terrain_score(self, campsite: CampsiteDTO, profile: UserProfile) -> float:
        if not profile.preferred_features:
            return 0.3
        features = campsite.natural_features
        if not features:
            return 0.3
        scores = [profile.preferred_features.get(f, 0.0) for f in features]
        return sum(scores) / len(scores)

    def _budget_score(self, campsite: CampsiteDTO, profile: UserProfile) -> float:
        avg = profile.avg_price_per_night
        if avg == 0.0:
            return 0.5
        diff = abs(campsite.price_per_night - avg) / avg
        return max(0.0, min(1.0, 1.0 - diff))

    def _rating_score(
        self,
        campsite: CampsiteDTO,
        reviews_map: dict[int, float],
    ) -> float:
        user_review = reviews_map.get(campsite.id)
        if user_review is not None:
            return max(0.0, min(1.0, user_review / 5.0))
        if campsite.global_avg_rating > 0.0:
            return max(0.0, min(1.0, campsite.global_avg_rating / 5.0))
        return 0.5

    def score_campsite(
        self,
        campsite: CampsiteDTO,
        profile: UserProfile,
        weather_score: float,
        current_month: int,
        reviews_map: dict[int, float],
    ) -> ScoreBreakdown:
        terrain = self._terrain_score(campsite, profile)
        budget = self._budget_score(campsite, profile)
        season = get_season_score(profile, current_month)
        rating = self._rating_score(campsite, reviews_map)
        weather = max(0.0, min(1.0, weather_score))

        w = self.WEIGHTS
        raw_total = (
            w["terrain"] * terrain
            + w["budget"] * budget
            + w["season"] * season
            + w["rating"] * rating
            + w["weather"] * weather
        ) * 100.0

        total = max(0.0, min(100.0, raw_total))

        return ScoreBreakdown(
            terrain=terrain,
            budget=budget,
            season=season,
            rating=rating,
            weather=weather,
            total=total,
        )

    def rank_campsites(
        self,
        campsites: list[CampsiteDTO],
        profile: UserProfile,
        weather_scores: dict[int, WeatherResult],
        current_month: int,
        reviews_map: dict[int, float],
    ) -> list[tuple[CampsiteDTO, ScoreBreakdown]]:
        results: list[tuple[CampsiteDTO, ScoreBreakdown]] = []

        for campsite in campsites:
            weather_result = weather_scores.get(campsite.id)
            w_score = weather_result.score if weather_result is not None else 0.5
            breakdown = self.score_campsite(
                campsite, profile, w_score, current_month, reviews_map
            )
            results.append((campsite, breakdown))

        results.sort(key=lambda x: x[1].total, reverse=True)
        return results[:5]
