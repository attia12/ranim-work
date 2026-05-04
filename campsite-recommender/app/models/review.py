from pydantic import BaseModel, Field


class ReviewHistory(BaseModel):
    campsite_id: int
    score: float = Field(..., ge=1.0, le=5.0)
