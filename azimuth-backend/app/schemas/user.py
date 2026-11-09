from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime
from app.schemas.recommend import Preference, TemperaturePreference


class PreferencesResponse(BaseModel):
    preferences: List[Preference]
    temperature_preference: Optional[TemperaturePreference] = None
    updated_at: Optional[datetime] = None


class PreferencesUpdateRequest(BaseModel):
    preferences: List[Preference]
    temperature_preference: Optional[TemperaturePreference] = None


class FavoriteItem(BaseModel):
    id: int
    name: str
    code: str
    capital: str
    flag_url: str
    added_at: datetime


class FavoriteResponse(BaseModel):
    message: str
    country_id: int
    added_at: Optional[datetime] = None


class FavoriteListResponse(BaseModel):
    favorites: List[FavoriteItem]
    total: int
