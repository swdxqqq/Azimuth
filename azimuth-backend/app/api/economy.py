from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.services.economy_service import EconomyService

router = APIRouter(prefix="/economy", tags=["Economy"])


@router.get("/{country_name}")
def get_country_economy(country_name: str, db: Session = Depends(get_db)):
    service = EconomyService(db)
    data = service.get_country_economy(country_name)
    
    if not data:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Economic data not found for {country_name}"
        )
    
    return data
