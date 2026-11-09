from app.schemas.country import (
    CountryBase,
    CountryListItem,
    CountryDetail,
    CountryListResponse,
    CountryDetailResponse,
)
from app.schemas.auth import RegisterRequest, LoginRequest, AuthResponse
from app.schemas.recommend import (
    Preference,
    TemperaturePreference,
    RecommendationRequest,
    RecommendationItem,
    RecommendationResponse,
)
from app.schemas.user import (
    PreferencesResponse,
    PreferencesUpdateRequest,
    FavoriteItem,
    FavoriteResponse,
    FavoriteListResponse,
)

__all__ = [
    "CountryBase",
    "CountryListItem",
    "CountryDetail",
    "CountryListResponse",
    "CountryDetailResponse",
    "RegisterRequest",
    "LoginRequest",
    "AuthResponse",
    "Preference",
    "TemperaturePreference",
    "RecommendationRequest",
    "RecommendationItem",
    "RecommendationResponse",
    "PreferencesResponse",
    "PreferencesUpdateRequest",
    "FavoriteItem",
    "FavoriteResponse",
    "FavoriteListResponse",
]
