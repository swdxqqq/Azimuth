from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    PROJECT_NAME: str = "Azimuth API"
    VERSION: str = "1.0.0"
    API_V1_STR: str = "/api/v1"
    
    DATABASE_URL: str
    JWT_SECRET_KEY: str
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 1440
    
    CURRENTS_API_KEY: Optional[str] = None
    RATE_LIMIT_DEFAULT: str = "100/minute"
    RATE_LIMIT_AUTHENTICATED: str = "1000/minute"
    
    CACHE_TTL_COUNTRIES: int = 3600
    CACHE_TTL_METRICS: int = 3600
    CACHE_TTL_NEWS: int = 43200
    CACHE_TTL_ECONOMY: int = 86400
    CACHE_TTL_RECOMMENDATIONS: int = 300
    
    LOG_LEVEL: str = "INFO"
    
    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()
