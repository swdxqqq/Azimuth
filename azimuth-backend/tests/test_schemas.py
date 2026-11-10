import pytest
from pydantic import ValidationError
from app.schemas.auth import RegisterRequest, LoginRequest, AuthResponse
from app.schemas.country import CountryBase, CountryListItem, CountryDetail, CountryListResponse
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
from datetime import datetime


class TestAuthSchemas:
    def test_register_valid(self, valid_register_payload):
        req = RegisterRequest(**valid_register_payload)
        assert req.email == "test@example.com"
        assert req.password == "SecurePass123!"

    def test_register_short_password(self):
        with pytest.raises(ValidationError):
            RegisterRequest(email="test@example.com", password="short")

    def test_register_invalid_email(self):
        with pytest.raises(ValidationError):
            RegisterRequest(email="not-an-email", password="SecurePass123!")

    def test_login_valid(self, valid_login_payload):
        req = LoginRequest(**valid_login_payload)
        assert req.email == "test@example.com"

    def test_auth_response(self):
        resp = AuthResponse(access_token="tok", expires_in=1800, user_id=1)
        assert resp.token_type == "bearer"
        assert resp.expires_in == 1800


class TestCountrySchemas:
    def test_country_base(self, sample_country_data):
        c = CountryBase(**sample_country_data)
        assert c.name == "Portugal"
        assert c.code == "PT"

    def test_country_list_item_with_metrics(self, sample_country_data, sample_metrics):
        data = {**sample_country_data, "metrics": sample_metrics}
        item = CountryListItem(**data)
        assert item.metrics["safety"] == 64.8

    def test_country_detail_full(self, sample_country_data, sample_metrics):
        data = {
            **sample_country_data,
            "metrics": sample_metrics,
            "economic_indicators": [{"gdp": 100}],
            "news": [{"title": "News"}],
        }
        detail = CountryDetail(**data)
        assert len(detail.economic_indicators) == 1

    def test_country_list_response(self):
        resp = CountryListResponse(countries=[], total=0, page=1, limit=10)
        assert resp.total == 0
        assert resp.limit == 10


class TestRecommendSchemas:
    def test_preference_valid(self):
        p = Preference(metric="safety", priority=8, enabled=True)
        assert p.priority == 8

    def test_preference_priority_too_low(self):
        with pytest.raises(ValidationError):
            Preference(metric="safety", priority=0)

    def test_preference_priority_too_high(self):
        with pytest.raises(ValidationError):
            Preference(metric="safety", priority=11)

    def test_recommendation_request(self):
        req = RecommendationRequest(
            preferences=[Preference(metric="safety", priority=5)],
            top_n=5,
        )
        assert req.top_n == 5

    def test_recommendation_request_top_n_too_high(self):
        with pytest.raises(ValidationError):
            RecommendationRequest(
                preferences=[Preference(metric="safety", priority=5)],
                top_n=100,
            )

    def test_recommendation_response_defaults(self):
        resp = RecommendationResponse(recommendations=[])
        assert resp.algorithm == "geometric_mean"
        assert resp.parameters["gamma"] == 1.3


class TestUserSchemas:
    def test_favorite_item(self):
        item = FavoriteItem(
            id=1, name="Portugal", code="PT", capital="Lisbon",
            flag_url="/flags/pt.svg", added_at=datetime.now(),
        )
        assert item.name == "Portugal"

    def test_favorite_response(self):
        resp = FavoriteResponse(message="Added", country_id=1)
        assert resp.message == "Added"

    def test_favorite_list_response(self):
        resp = FavoriteListResponse(favorites=[], total=0)
        assert resp.total == 0

    def test_preferences_update_request(self):
        req = PreferencesUpdateRequest(
            preferences=[Preference(metric="safety", priority=7)],
            temperature_preference=TemperaturePreference(enabled=True, target=22.0),
        )
        assert len(req.preferences) == 1
