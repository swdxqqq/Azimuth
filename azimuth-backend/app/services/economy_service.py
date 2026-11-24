from typing import Dict, Optional
from datetime import datetime, timedelta, timezone
from sqlalchemy.orm import Session
from app.models.metrics import EconomicIndicator
from app.clients.imf_client import IMFClient


class EconomyService:
    HISTORY_START_YEAR = 2020
    HISTORY_END_YEAR = 2024
    INDICATOR_TYPES = ("gdp_per_capita", "inflation", "unemployment")

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

        response = {**api_data, "country_id": country.id}
        self._save_to_cache(country.id, response)
        return response

    def _get_from_cache(self, country_id: int) -> Optional[Dict]:
        indicators = self.db.query(EconomicIndicator).filter(
            EconomicIndicator.country_id == country_id
        ).all()

        if not indicators:
            return None

        historical = [
            indicator
            for indicator in indicators
            if self.HISTORY_START_YEAR <= indicator.year <= self.HISTORY_END_YEAR
            and indicator.indicator_type in self.INDICATOR_TYPES
        ]
        if not historical:
            return None

        latest = max(
            (indicator for indicator in historical if indicator.updated_at is not None),
            key=lambda indicator: indicator.updated_at,
            default=None,
        )
        if latest is None:
            return None
        now = datetime.now(timezone.utc)
        updated_at = latest.updated_at
        if updated_at.tzinfo is None:
            updated_at = updated_at.replace(tzinfo=timezone.utc)
        if now - updated_at > timedelta(days=30):
            return None

        values_by_year = {}
        for indicator in historical:
            year_values = values_by_year.setdefault(
                indicator.year,
                {"year": indicator.year, **{name: None for name in self.INDICATOR_TYPES}},
            )
            year_values[indicator.indicator_type] = indicator.value

        history = [values_by_year[year] for year in sorted(values_by_year)]
        latest_year = history[-1]
        return {"country_id": country_id, **latest_year, "history": history}

    def _save_to_cache(self, country_id: int, data: Dict):
        history = data.get("history") or [{
            "year": data.get("year", self.HISTORY_END_YEAR),
            **{name: data.get(name) for name in self.INDICATOR_TYPES},
        }]
        valid_history = []
        for point in history:
            try:
                year = int(point["year"])
            except (KeyError, TypeError, ValueError):
                continue
            if self.HISTORY_START_YEAR <= year <= self.HISTORY_END_YEAR:
                valid_history.append({**point, "year": year})

        if not valid_history:
            return

        self.db.query(EconomicIndicator).filter(
            EconomicIndicator.country_id == country_id,
            EconomicIndicator.indicator_type.in_(self.INDICATOR_TYPES),
        ).delete()

        for point in valid_history:
            year = point["year"]
            for indicator_type in self.INDICATOR_TYPES:
                value = point.get(indicator_type)
                if value is None:
                    continue
                self.db.add(EconomicIndicator(
                    country_id=country_id,
                    year=year,
                    indicator_type=indicator_type,
                    value=float(value),
                    source="IMF WEO",
                ))

        self.db.commit()
