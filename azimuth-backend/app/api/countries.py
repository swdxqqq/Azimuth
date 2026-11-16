from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from typing import Optional
from app.database import get_db
from app.models.country import Country, Metric
from app.models.metrics import EconomicIndicator, NewsArticle
from app.schemas.country import CountryListResponse, CountryDetailResponse, CountryListItem

router = APIRouter(prefix="/countries", tags=["Countries"])


@router.get("", response_model=CountryListResponse)
def get_countries(
    page: int = Query(default=1, ge=1),
    limit: int = Query(default=20, ge=1, le=100),
    region: Optional[str] = Query(default=None),
    sort_by: Optional[str] = Query(default="name"),
    order: Optional[str] = Query(default="asc"),
    db: Session = Depends(get_db)
):
    query = db.query(Country)
    
    if region:
        query = query.filter(Country.region == region)
    
    total = query.count()
    
    if sort_by == "name":
        if order == "desc":
            query = query.order_by(Country.name.desc())
        else:
            query = query.order_by(Country.name.asc())
    
    countries = query.offset((page - 1) * limit).limit(limit).all()
    
    result = []
    for country in countries:
        latest_metrics = db.query(Metric).filter(
            Metric.country_id == country.id,
            Metric.year == db.query(Metric.year).filter(Metric.country_id == country.id).order_by(Metric.year.desc()).first()[0]
        ).all()
        
        metrics_dict = {m.metric_type: m.value for m in latest_metrics}
        
        result.append(CountryListItem(
            id=country.id,
            name=country.name,
            code=country.code,
            capital=country.capital,
            region=country.region,
            flag_url=country.flag_url,
            metrics=metrics_dict
        ))
    
    return CountryListResponse(
        countries=result,
        total=total,
        page=page,
        limit=limit
    )


@router.get("/{country_id}", response_model=CountryDetailResponse)
def get_country(country_id: int, db: Session = Depends(get_db)):
    country = db.query(Country).filter(Country.id == country_id).first()
    if not country:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Country not found"
        )
    
    latest_year = db.query(Metric.year).filter(Metric.country_id == country_id).order_by(Metric.year.desc()).first()
    if latest_year:
        latest_metrics = db.query(Metric).filter(
            Metric.country_id == country_id,
            Metric.year == latest_year[0]
        ).all()
        metrics_dict = {m.metric_type: m.value for m in latest_metrics}
    else:
        metrics_dict = {}
    
    econ_indicators = db.query(EconomicIndicator).filter(
        EconomicIndicator.country_id == country_id
    ).all()
    
    econ_dict = {}
    for ei in econ_indicators:
        econ_dict[ei.indicator_type] = {
            "value": ei.value,
            "year": ei.year,
            "source": ei.source
        }
    
    news_articles = db.query(NewsArticle).filter(
        NewsArticle.country_id == country_id
    ).order_by(NewsArticle.published_at.desc()).limit(10).all()
    
    news_list = [
        {
            "title": a.title,
            "description": a.description,
            "url": a.url,
            "category": a.category,
            "published_at": a.published_at.isoformat() if a.published_at else None,
            "source": a.source
        }
        for a in news_articles
    ]
    
    return CountryDetailResponse(
        id=country.id,
        name=country.name,
        code=country.code,
        capital=country.capital,
        region=country.region,
        flag_url=country.flag_url,
        latitude=country.latitude,
        longitude=country.longitude,
        metrics=metrics_dict,
        economic_indicators=econ_dict,
        news=news_list
    )
