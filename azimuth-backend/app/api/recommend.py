from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.country import Country
from app.schemas.recommend import RecommendationRequest, RecommendationResponse, RecommendationItem
from app.services.recsys_adapter import RecSysAdapter

router = APIRouter(prefix="/recommend", tags=["Recommendations"])


@router.post("", response_model=RecommendationResponse)
def get_recommendations(request: RecommendationRequest, db: Session = Depends(get_db)):
    adapter = RecSysAdapter(db)

    temp_pref = None
    if request.temperature_preference:
        temp_pref = {
            "enabled": request.temperature_preference.enabled,
            "target_temp": request.temperature_preference.target
        }

    preferences = [p.dict() for p in request.preferences]

    results = adapter.recommend(preferences, temp_pref, request.top_n)

    # Get country IDs mapping
    countries = db.query(Country).all()
    country_id_map = {c.name: c.id for c in countries}

    recommendations = []
    for _, row in results.iterrows():
        country_name = row["Country"]
        country_id = country_id_map.get(country_name, 0)
        
        recommendations.append(RecommendationItem(
            country_id=country_id,
            name=country_name,
            score=float(row["Final_Score"]),
            breakdown={},
            confidence=float(row.get("Confidence", 1.0))
        ))

    return RecommendationResponse(
        recommendations=recommendations,
        algorithm="geometric_mean",
        parameters={"gamma": 1.3, "epsilon": 0.02, "lambda": 0.08}
    )
