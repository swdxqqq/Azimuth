from typing import Dict, Optional
import time
from imfp import imf_get
from ..constants import COUNTRY_ISO3
import warnings

warnings.filterwarnings('ignore')


class IMFClient:
    def __init__(self, cache_ttl: int = 86400):
        self.cache_ttl = cache_ttl
        self._cache: Dict[str, tuple] = {}
        self.year = 2025

    def get_country_economy(self, country: str) -> Optional[Dict[str, Optional[float]]]:
        iso3 = COUNTRY_ISO3.get(country)
        if not iso3:
            return None

        cache_key = f"{iso3}_{self.year}"
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

            latest = df[df['TIME_PERIOD'] == str(self.year)]
            result = {
                "country": country,
                "iso3": iso3,
                "year": self.year,
                "gdp_per_capita": None,
                "inflation": None,
                "unemployment": None,
            }

            for _, row in latest.iterrows():
                indicator = row['INDICATOR']
                value = float(row['OBS_VALUE'])
                if indicator == 'NGDPDPC':
                    result["gdp_per_capita"] = value
                elif indicator == 'PCPIPCH':
                    result["inflation"] = value
                elif indicator == 'LUR':
                    result["unemployment"] = value

            self._cache[cache_key] = (time.time(), result)
            return result
        except Exception:
            return None

    def clear_cache(self):
        self._cache.clear()
