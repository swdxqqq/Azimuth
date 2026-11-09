from pydantic import BaseModel, Field
from typing import List, Optional, Dict
from datetime import datetime


class Preference(BaseModel):
    metric: str
    priority: int = Field(..., ge=1, le=10)
    enabled: bool = True


class TemperaturePreference(BaseModel):
    target: float
    enabled: bool = True


class RecommendationRequest(BaseModel):
    preferences: List[Preference]
    temperature_preference: Optional[TemperaturePreference] = None
    top_n: int = Field(default=10, ge=1, le=50)


class RecommendationItem(BaseModel):
    country_id: int
    name: str
    score: float
    breakdown: Dict[str, float]
    confidence: float


class RecommendationResponse(BaseModel):
    recommendations: List[RecommendationItem]
    algorithm: str = "geometric_mean"
    parameters: dict = {
        "gamma": 1.3,
        "epsilon": 0.02,
        "lambda": 0.08
    }
