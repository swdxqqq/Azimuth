from typing import List, Optional
from datetime import datetime, timedelta, timezone
from sqlalchemy.orm import Session
from app.models.metrics import NewsArticle
from app.models.country import Country
from app.clients.currents import CurrentsClient
from app.config import settings


class NewsService:
    def __init__(self, db: Session):
        self.db = db
        self.currents_client = CurrentsClient(settings.CURRENTS_API_KEY)

    def get_country_news(self, country_name: str, limit: int = 10) -> Optional[List[dict]]:
        country = self.db.query(Country).filter(Country.name == country_name).first()
        if not country:
            return None

        cached = self._get_from_cache(country.id)
        if cached:
            return cached[:limit]

        api_data = self.currents_client.get_latest_news(country=country_name, page_size=20)
        if not api_data:
            return None

        self._save_to_cache(country.id, api_data)
        
        cached = self._get_from_cache(country.id)
        return cached[:limit] if cached else None

    def _get_from_cache(self, country_id: int) -> Optional[List[dict]]:
        articles = self.db.query(NewsArticle).filter(
            NewsArticle.country_id == country_id
        ).order_by(NewsArticle.published_at.desc()).all()

        if not articles:
            return None

        latest = articles[0]
        now = datetime.now(timezone.utc)
        published_at = latest.published_at
        if published_at.tzinfo is None:
            published_at = published_at.replace(tzinfo=timezone.utc)
        if now - published_at > timedelta(hours=6):
            return None

        return [
            {
                "title": a.title,
                "description": a.description,
                "url": a.url,
                "category": a.category,
                "published_at": a.published_at.isoformat() if a.published_at else None,
                "source": a.source
            }
            for a in articles
        ]

    def _save_to_cache(self, country_id: int, articles: List[dict]):
        self.db.query(NewsArticle).filter(
            NewsArticle.country_id == country_id
        ).delete()

        for article in articles:
            published_str = article.get("published", "")
            try:
                published_at = datetime.fromisoformat(published_str.replace("Z", "+00:00"))
            except (ValueError, AttributeError):
                published_at = datetime.now(timezone.utc)

            categories = article.get("category", [])
            category = categories[0] if categories else None

            self.db.add(NewsArticle(
                country_id=country_id,
                title=article.get("title", "")[:500],
                description=article.get("description"),
                url=article.get("url", ""),
                language=article.get("language"),
                category=category,
                published_at=published_at,
                source=article.get("source", {}).get("name") if isinstance(article.get("source"), dict) else str(article.get("source", ""))[:200]
            ))

        self.db.commit()
