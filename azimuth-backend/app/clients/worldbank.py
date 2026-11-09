from typing import Dict, Optional
from .base import HttpClient
from ..constants import COUNTRY_ISO3


class WorldBankClient:
    def __init__(self, cache_ttl: int = 86400):
        self.base_url = "https://api.worldbank.org/v2"
        self.client = HttpClient(cache_ttl=cache_ttl)

    def get_indicator(
        self, country_iso3: str, indicator: str, year: int = 2023
    ) -> Optional[float]:
        url = f"{self.base_url}/country/{country_iso3}/indicator/{indicator}?format=json&date={year}&per_page=1"
        data = self.client.get(url)
        if data and isinstance(data, list) and len(data) == 2 and data[1] and len(data[1]) > 0:
            return data[1][0].get("value")
        return None

    def get_gdp_per_capita(self, country_iso3: str, year: int = 2023) -> Optional[float]:
        return self.get_indicator(country_iso3, "NY.GDP.PCAP.CD", year)

    def get_inflation(self, country_iso3: str, year: int = 2023) -> Optional[float]:
        return self.get_indicator(country_iso3, "FP.CPI.TOTL.ZG", year)

    def get_unemployment(self, country_iso3: str, year: int = 2023) -> Optional[float]:
        return self.get_indicator(country_iso3, "SL.UEM.TOTL.ZS", year)

    def get_country_iso3(self, country: str) -> Optional[str]:
        return COUNTRY_ISO3.get(country)

    def get_country_economy(
        self, country: str, year: int = 2023
    ) -> Optional[Dict[str, Optional[float]]]:
        iso3 = self.get_country_iso3(country)
        if not iso3:
            return None
        return {
            "country": country,
            "iso3": iso3,
            "year": year,
            "gdp_per_capita": self.get_gdp_per_capita(iso3, year),
            "inflation": self.get_inflation(iso3, year),
            "unemployment": self.get_unemployment(iso3, year),
        }
