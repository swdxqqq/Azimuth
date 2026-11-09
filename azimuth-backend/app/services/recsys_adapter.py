from typing import Dict, List
import pandas as pd
from sqlalchemy.orm import Session
from app.models.country import Country, Metric
from app.recsys.config import UserPreference, TemperaturePreference, CriterionDirection
from app.recsys.engine import RecSysEngine


class RecSysAdapter:
    def __init__(self, db: Session, year: int = 2025):
        self.db = db
        self.year = year

    def _load_data_from_db(self) -> pd.DataFrame:
        countries = self.db.query(Country).all()
        data = []

        for country in countries:
            metrics = self.db.query(Metric).filter(
                Metric.country_id == country.id,
                Metric.year == self.year
            ).all()

            if not metrics:
                continue

            row = {"Country": country.name}
            for metric in metrics:
                row[metric.metric_type] = metric.value
            data.append(row)

        return pd.DataFrame(data)

    def recommend(
        self,
        preferences: List[Dict],
        temperature_pref: Dict = None,
        top_n: int = 10
    ) -> pd.DataFrame:
        user_prefs = {}
        for pref in preferences:
            user_prefs[pref["metric"]] = UserPreference(
                priority=pref["priority"],
                enabled=pref["enabled"]
            )

        temp_pref = None
        if temperature_pref:
            temp_pref = TemperaturePreference(
                enabled=temperature_pref.get("enabled", False),
                target_temp=temperature_pref.get("target_temp", 20)
            )

        engine = RecSysEngine(year=self.year)
        engine._raw_df = self._load_data_from_db()

        return engine.recommend(user_prefs, temp_pref, top_n)
