from datetime import date
from pydantic import BaseModel
from app.models.campsite import NaturalFeature


class BookingHistory(BaseModel):
    campsite_id: int
    campsite_name: str
    natural_features: list[NaturalFeature]
    price_per_night: float
    check_in: date
    check_out: date
    latitude: float
    longitude: float
