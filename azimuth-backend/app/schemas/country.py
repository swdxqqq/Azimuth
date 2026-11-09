from pydantic import BaseModel, ConfigDict
from typing import Optional, List
from datetime import datetime


class CountryBase(BaseModel):
    id: int
    name: str
    code: str
    capital: str
    region: str
    flag_url: str


class CountryListItem(CountryBase):
    model_config = ConfigDict(from_attributes=True)
    
    metrics: Optional[dict] = None


class CountryDetail(CountryBase):
    model_config = ConfigDict(from_attributes=True)
    
    latitude: float
    longitude: float
    metrics: Optional[dict] = None
    economic_indicators: Optional[List[dict]] = None
    news: Optional[List[dict]] = None


class CountryListResponse(BaseModel):
    countries: List[CountryListItem]
    total: int
    page: int
    limit: int


class CountryDetailResponse(CountryDetail):
    pass
