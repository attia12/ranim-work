from pydantic import BaseModel
from app.models.campsite import NaturalFeature, CampsiteDTO
from app.models.booking import BookingHistory
from app.models.review import ReviewHistory


class ScoreBreakdown(BaseModel):
    terrain: float
    budget: float
    season: float
    rating: float
    weather: float
    total: float


class RecommendationRequest(BaseModel):
    user_id: int
    bookings: list[BookingHistory]
    reviews: list[ReviewHistory]
    all_campsites: list[CampsiteDTO]


class RecommendedCampsite(BaseModel):
    campsite_id: int
    campsite_name: str
    score: float
    score_breakdown: ScoreBreakdown
    weather_description: str
    natural_features: list[NaturalFeature]
    price_per_night: float
    image_url: str | None = None
