from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from datetime import datetime
from app.database import get_db
from app.dependencies import get_current_user
from app.models.user import User, UserPreference, FavoriteCountry
from app.models.country import Country
from app.schemas.user import (
    PreferencesResponse,
    PreferencesUpdateRequest,
    FavoriteResponse,
    FavoriteListResponse,
    FavoriteItem
)

router = APIRouter(prefix="/user", tags=["User"])


@router.get("/preferences", response_model=PreferencesResponse)
def get_preferences(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    prefs = db.query(UserPreference).filter(UserPreference.user_id == current_user.id).all()
    
    return PreferencesResponse(
        preferences=[
            {
                "metric": p.metric_type,
                "priority": p.priority,
                "enabled": p.enabled
            }
            for p in prefs
        ],
        temperature_preference=None,
        updated_at=None
    )


@router.put("/preferences", response_model=PreferencesResponse)
def update_preferences(
    request: PreferencesUpdateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    db.query(UserPreference).filter(UserPreference.user_id == current_user.id).delete()
    
    for pref in request.preferences:
        user_pref = UserPreference(
            user_id=current_user.id,
            metric_type=pref.metric,
            priority=pref.priority,
            enabled=pref.enabled
        )
        db.add(user_pref)
    
    db.commit()
    
    return PreferencesResponse(
        preferences=request.preferences,
        temperature_preference=request.temperature_preference,
        updated_at=datetime.utcnow()
    )


@router.get("/favorites", response_model=FavoriteListResponse)
def get_favorites(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    favorites = db.query(FavoriteCountry).filter(FavoriteCountry.user_id == current_user.id).all()
    
    items = []
    for fav in favorites:
        country = db.query(Country).filter(Country.id == fav.country_id).first()
        if country:
            items.append(FavoriteItem(
                id=country.id,
                name=country.name,
                code=country.code,
                capital=country.capital,
                flag_url=country.flag_url,
                added_at=fav.created_at
            ))
    
    return FavoriteListResponse(
        favorites=items,
        total=len(items)
    )


@router.post("/favorites/{country_id}", response_model=FavoriteResponse)
def add_favorite(
    country_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    country = db.query(Country).filter(Country.id == country_id).first()
    if not country:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Country not found"
        )
    
    existing = db.query(FavoriteCountry).filter(
        FavoriteCountry.user_id == current_user.id,
        FavoriteCountry.country_id == country_id
    ).first()
    
    if existing:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Country already in favorites"
        )
    
    favorite = FavoriteCountry(
        user_id=current_user.id,
        country_id=country_id
    )
    db.add(favorite)
    db.commit()
    db.refresh(favorite)
    
    return FavoriteResponse(
        message="Country added to favorites",
        country_id=country_id,
        added_at=favorite.created_at
    )


@router.delete("/favorites/{country_id}", response_model=FavoriteResponse)
def remove_favorite(
    country_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    favorite = db.query(FavoriteCountry).filter(
        FavoriteCountry.user_id == current_user.id,
        FavoriteCountry.country_id == country_id
    ).first()
    
    if not favorite:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Country not in favorites"
        )
    
    db.delete(favorite)
    db.commit()
    
    return FavoriteResponse(
        message="Country removed from favorites",
        country_id=country_id
    )
