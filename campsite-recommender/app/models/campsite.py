from enum import Enum
from pydantic import BaseModel


class NaturalFeature(str, Enum):
    FOREST = "FOREST"
    LAKE = "LAKE"
    MOUNTAIN = "MOUNTAIN"
    BEACH = "BEACH"
    RIVER = "RIVER"
    PLAIN = "PLAIN"


class CampsiteDTO(BaseModel):
    id: int
    name: str
    natural_features: list[NaturalFeature]
    price_per_night: float
    latitude: float
    longitude: float
    global_avg_rating: float = 0.0
    image_url: str | None = None
