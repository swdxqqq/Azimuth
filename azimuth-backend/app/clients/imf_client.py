import math
import time
from typing import Dict, Optional

from imfp import imf_get
from ..constants import COUNTRY_ISO3
import warnings

warnings.filterwarnings('ignore')


class IMFClient:
    HISTORY_START_YEAR = 2020
    HISTORY_END_YEAR = 2024
    INDICATOR_FIELDS = {
        "NGDPDPC": "gdp_per_capita",
        "PCPIPCH": "inflation",
        "LUR": "unemployment",
    }

    def __init__(self, cache_ttl: int = 86400):
        self.cache_ttl = cache_ttl
        self._cache: Dict[str, tuple] = {}
        self.year = self.HISTORY_END_YEAR

    def get_country_economy(self, country: str) -> Optional[Dict]:
        iso3 = COUNTRY_ISO3.get(country)
        if not iso3:
            return None

        cache_key = f"{iso3}_{self.HISTORY_START_YEAR}_{self.HISTORY_END_YEAR}"
        if cache_key in self._cache:
            cached_time, cached_data = self._cache[cache_key]
            if time.time() - cached_time < self.cache_ttl:
                return cached_data

        try:
            df = imf_get(
                dataflow_id='WEO',
                dimensions={
                    'COUNTRY': iso3,
                    'INDICATOR': ['NGDPDPC', 'PCPIPCH', 'LUR'],
                    'FREQUENCY': 'A'
                }
            )

            values_by_year = {
                year: {
                    "year": year,
                    "gdp_per_capita": None,
                    "inflation": None,
                    "unemployment": None,
                }
                for year in range(self.HISTORY_START_YEAR, self.HISTORY_END_YEAR + 1)
            }

            for _, row in df.iterrows():
                try:
                    year = int(float(row["TIME_PERIOD"]))
                except (KeyError, TypeError, ValueError):
                    continue
                if year not in values_by_year:
                    continue

                field = self.INDICATOR_FIELDS.get(row.get("INDICATOR"))
                if field is None:
                    continue
                try:
                    value = float(row["OBS_VALUE"])
                except (KeyError, TypeError, ValueError):
                    continue
                if math.isfinite(value):
                    values_by_year[year][field] = value

            history = [
                point
                for point in values_by_year.values()
                if any(point[field] is not None for field in self.INDICATOR_FIELDS.values())
            ]
            if not history:
                return None

            latest = history[-1]
            result = {
                "country": country,
                "iso3": iso3,
                **latest,
                "history": history,
            }

            self._cache[cache_key] = (time.time(), result)
            return result
        except Exception:
            return None

    def clear_cache(self):
        self._cache.clear()
