from sqlalchemy import Column, Integer, String, DateTime, Boolean, ForeignKey, UniqueConstraint
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String(255), nullable=False, unique=True, index=True)
    password_hash = Column(String(255), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    last_login = Column(DateTime(timezone=True))

    preferences = relationship("UserPreference", back_populates="user", cascade="all, delete-orphan")
    favorites = relationship("FavoriteCountry", back_populates="user", cascade="all, delete-orphan")


class UserPreference(Base):
    __tablename__ = "user_preferences"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    metric_type = Column(String(50), nullable=False)
    priority = Column(Integer, nullable=False)
    enabled = Column(Boolean, default=True, nullable=False)

    user = relationship("User", back_populates="preferences")

    __table_args__ = (
        UniqueConstraint('user_id', 'metric_type', name='uq_user_metric'),
    )


class FavoriteCountry(Base):
    __tablename__ = "favorite_countries"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    country_id = Column(Integer, ForeignKey("countries.id"), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="favorites")
    country = relationship("Country", back_populates="favorites")

    __table_args__ = (
        UniqueConstraint('user_id', 'country_id', name='uq_user_country'),
    )
