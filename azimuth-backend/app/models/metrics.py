from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, Text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base


class EconomicIndicator(Base):
    __tablename__ = "economic_indicators"

    id = Column(Integer, primary_key=True, index=True)
    country_id = Column(Integer, ForeignKey("countries.id"), nullable=False)
    year = Column(Integer, nullable=False)
    indicator_type = Column(String(50), nullable=False)
    value = Column(Float, nullable=False)
    source = Column(String(100))
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    country = relationship("Country", back_populates="economic_indicators")

    __table_args__ = (
        {'mysql_engine': 'InnoDB'},
    )


class NewsArticle(Base):
    __tablename__ = "news_articles"

    id = Column(Integer, primary_key=True, index=True)
    country_id = Column(Integer, ForeignKey("countries.id"), nullable=False)
    title = Column(String(500), nullable=False)
    description = Column(Text)
    url = Column(String(1000), nullable=False)
    language = Column(String(10))
    category = Column(String(50))
    published_at = Column(DateTime(timezone=True), nullable=False)
    source = Column(String(200))

    country = relationship("Country", back_populates="news_articles")
