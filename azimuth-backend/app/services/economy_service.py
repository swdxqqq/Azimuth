from typing import Dict, Optional
from datetime import datetime, timedelta, timezone
from sqlalchemy.orm import Session
from app.models.metrics import EconomicIndicator
from app.clients.imf_client import IMFClient


class EconomyService:
    def __init__(self, db: Session):
        self.db = db
        self.imf_client = IMFClient()

    def get_country_economy(self, country_name: str) -> Optional[Dict]:
        from app.models.country import Country
        country = self.db.query(Country).filter(Country.name == country_name).first()
        if not country:
            return None

        cached = self._get_from_cache(country.id)
        if cached:
            return cached

        api_data = self.imf_client.get_country_economy(country_name)
        if not api_data:
            return None

        self._save_to_cache(country.id, api_data)
        return api_data

    def _get_from_cache(self, country_id: int) -> Optional[Dict]:
        indicators = self.db.query(EconomicIndicator).filter(
            EconomicIndicator.country_id == country_id
        ).all()

        if not indicators:
            return None

        latest = max(indicators, key=lambda x: x.updated_at)
        now = datetime.now(timezone.utc)
        updated_at = latest.updated_at
        if updated_at.tzinfo is None:
            updated_at = updated_at.replace(tzinfo=timezone.utc)
        if now - updated_at > timedelta(days=30):
            return None

        result = {
            "country_id": country_id,
            "year": indicators[0].year,
            "gdp_per_capita": None,
            "inflation": None,
            "unemployment": None,
        }

        for ind in indicators:
            if ind.indicator_type == "gdp_per_capita":
                result["gdp_per_capita"] = ind.value
            elif ind.indicator_type == "inflation":
                result["inflation"] = ind.value
            elif ind.indicator_type == "unemployment":
                result["unemployment"] = ind.value

        return result

    def _save_to_cache(self, country_id: int, data: Dict):
        self.db.query(EconomicIndicator).filter(
            EconomicIndicator.country_id == country_id
        ).delete()

        year = data.get("year", 2025)
        
        if data.get("gdp_per_capita") is not None:
            self.db.add(EconomicIndicator(
                country_id=country_id,
                year=year,
                indicator_type="gdp_per_capita",
                value=data["gdp_per_capita"],
                source="IMF WEO"
            ))

        if data.get("inflation") is not None:
            self.db.add(EconomicIndicator(
                country_id=country_id,
                year=year,
                indicator_type="inflation",
                value=data["inflation"],
                source="IMF WEO"
            ))

        if data.get("unemployment") is not None:
            self.db.add(EconomicIndicator(
                country_id=country_id,
                year=year,
                indicator_type="unemployment",
                value=data["unemployment"],
                source="IMF WEO"
            ))

        self.db.commit()
