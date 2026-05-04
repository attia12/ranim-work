from enum import Enum
from pydantic import BaseModel
from app.models.campsite import NaturalFeature
from app.models.booking import BookingHistory
from app.models.review import ReviewHistory


class Season(str, Enum):
    SPRING = "SPRING"
    SUMMER = "SUMMER"
    AUTUMN = "AUTUMN"
    WINTER = "WINTER"


SEASON_MONTHS: dict[Season, list[int]] = {
    Season.SPRING: [3, 4, 5],
    Season.SUMMER: [6, 7, 8],
    Season.AUTUMN: [9, 10, 11],
    Season.WINTER: [12, 1, 2],
}

ADJACENT_SEASONS: dict[Season, list[Season]] = {
    Season.SPRING: [Season.WINTER, Season.SUMMER],
    Season.SUMMER: [Season.SPRING, Season.AUTUMN],
    Season.AUTUMN: [Season.SUMMER, Season.WINTER],
    Season.WINTER: [Season.AUTUMN, Season.SPRING],
}


class UserProfile(BaseModel):
    preferred_features: dict[NaturalFeature, float]
    avg_price_per_night: float
    preferred_season: Season
    avg_given_rating: float


def build_user_profile(bookings: list[BookingHistory], reviews: list[ReviewHistory]) -> UserProfile:
    # --- Terrain preferences ---
    feature_counts: dict[NaturalFeature, int] = {}
    for booking in bookings:
        for feature in booking.natural_features:
            feature_counts[feature] = feature_counts.get(feature, 0) + 1

    if feature_counts:
        max_count = max(feature_counts.values())
        preferred_features: dict[NaturalFeature, float] = {
            f: count / max_count for f, count in feature_counts.items()
        }
    else:
        preferred_features = {}

    # --- Average price ---
    if bookings:
        avg_price = sum(b.price_per_night for b in bookings) / len(bookings)
    else:
        avg_price = 50.0

    # --- Preferred season ---
    season_counts: dict[Season, int] = {}
    for booking in bookings:
        month = booking.check_in.month
        for season, months in SEASON_MONTHS.items():
            if month in months:
                season_counts[season] = season_counts.get(season, 0) + 1
                break

    if season_counts:
        preferred_season = max(season_counts, key=season_counts.__getitem__)
    else:
        preferred_season = Season.SUMMER

    # --- Average rating given ---
    if reviews:
        avg_rating = sum(r.score for r in reviews) / len(reviews)
    else:
        avg_rating = 3.0

    return UserProfile(
        preferred_features=preferred_features,
        avg_price_per_night=avg_price,
        preferred_season=preferred_season,
        avg_given_rating=avg_rating,
    )


def get_season_score(profile: UserProfile, current_month: int) -> float:
    current_season: Season | None = None
    for season, months in SEASON_MONTHS.items():
        if current_month in months:
            current_season = season
            break

    if current_season is None:
        return 0.5

    if current_season == profile.preferred_season:
        return 1.0
    if current_season in ADJACENT_SEASONS[profile.preferred_season]:
        return 0.5
    return 0.2
