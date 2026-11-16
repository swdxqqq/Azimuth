from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from app.database import get_db
from app.services.news_service import NewsService

router = APIRouter(prefix="/news", tags=["News"])


@router.get("/{country_name}")
def get_country_news(
    country_name: str,
    limit: int = Query(default=10, ge=1, le=20),
    db: Session = Depends(get_db)
):
    service = NewsService(db)
    news = service.get_country_news(country_name, limit)
    
    if news is None:
        raise HTTPException(status_code=404, detail=f"News not found for {country_name}")
    
    return {
        "country": country_name,
        "count": len(news),
        "articles": news
    }
