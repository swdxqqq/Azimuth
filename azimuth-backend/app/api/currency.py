from fastapi import APIRouter, HTTPException, Query
from typing import Optional
from app.clients.frankfurter import FrankfurterClient

router = APIRouter(prefix="/currency", tags=["Currency"])
client = FrankfurterClient()


@router.get("/rates/{currency}")
def get_rate_to_usd(currency: str):
    rate = client.get_rate_to_usd(currency.upper())
    if rate is None:
        raise HTTPException(status_code=404, detail=f"Rate not found for {currency}")
    return {"currency": currency.upper(), "rate_to_usd": rate}


@router.get("/rates/country/{country_name}")
def get_country_rate_to_usd(country_name: str):
    currency_code = client.get_country_currency(country_name)
    rate = client.get_country_rate_to_usd(country_name)
    if currency_code is None or rate is None:
        raise HTTPException(status_code=404, detail=f"Currency/rate not found for {country_name}")
    return {
        "country": country_name,
        "currency_code": currency_code,
        "rate_to_usd": rate,
    }
