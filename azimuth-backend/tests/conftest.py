import os
import pytest
from unittest.mock import patch, MagicMock
from datetime import timedelta

os.environ.setdefault("DATABASE_URL", "postgresql://test:test@localhost/test")
os.environ.setdefault("JWT_SECRET_KEY", "test-secret-key-for-testing-only")
os.environ.setdefault("JWT_ALGORITHM", "HS256")
os.environ.setdefault("ACCESS_TOKEN_EXPIRE_MINUTES", "30")


@pytest.fixture
def mock_settings():
    with patch("app.config.settings") as m:
        m.JWT_SECRET_KEY = "test-secret-key-for-testing-only"
        m.JWT_ALGORITHM = "HS256"
        m.ACCESS_TOKEN_EXPIRE_MINUTES = 30
        yield m


@pytest.fixture
def sample_country_data():
    return {
        "id": 1,
        "name": "Portugal",
        "code": "PT",
        "capital": "Lisbon",
        "region": "Europe",
        "flag_url": "/assets/flags/pt.svg",
        "latitude": 38.7223,
        "longitude": -9.1393,
    }


@pytest.fixture
def sample_metrics():
    return {
        "crime": 35.2,
        "safety": 64.8,
        "healthcare": 72.5,
        "pollution": 28.1,
        "cost_of_living": 55.3,
        "purchasing_power": 45.6,
        "climate": 78.2,
        "internet_speed": 85.4,
    }


@pytest.fixture
def valid_register_payload():
    return {"email": "test@example.com", "password": "SecurePass123!"}


@pytest.fixture
def valid_login_payload():
    return {"email": "test@example.com", "password": "SecurePass123!"}


@pytest.fixture
def mock_db():
    db = MagicMock()
    return db
