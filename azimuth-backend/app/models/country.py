from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, JSON
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base


class Country(Base):
    __tablename__ = "countries"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False, unique=True)
    code = Column(String(2), nullable=False, unique=True)
    capital = Column(String(100), nullable=False)
    region = Column(String(50), nullable=False)
    flag_url = Column(String(500), nullable=False)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    metrics = relationship("Metric", back_populates="country", cascade="all, delete-orphan")
    economic_indicators = relationship("EconomicIndicator", back_populates="country", cascade="all, delete-orphan")
    news_articles = relationship("NewsArticle", back_populates="country", cascade="all, delete-orphan")
    favorites = relationship("FavoriteCountry", back_populates="country", cascade="all, delete-orphan")


class Metric(Base):
    __tablename__ = "metrics"

    id = Column(Integer, primary_key=True, index=True)
    country_id = Column(Integer, ForeignKey("countries.id"), nullable=False)
    year = Column(Integer, nullable=False)
    metric_type = Column(String(50), nullable=False)
    value = Column(Float, nullable=False)
    source = Column(String(100))
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    country = relationship("Country", back_populates="metrics")

    __table_args__ = (
        {'mysql_engine': 'InnoDB'},
    )
