from typing import Dict, List, Optional
from .base import HttpClient
from ..constants import COUNTRY_CURRENCY


class FrankfurterClient:
    def __init__(self, cache_ttl: int = 86400):
        self.base_url = "https://api.frankfurter.dev/v2"
        self.client = HttpClient(cache_ttl=cache_ttl)

    def get_supported_currencies(self) -> Optional[List[str]]:
        url = f"{self.base_url}/currencies"
        data = self.client.get(url)
        if data and isinstance(data, list):
            return [item["iso_code"] for item in data if "iso_code" in item]
        return None

    def get_latest_rates(
        self, base: str = "USD", quotes: Optional[List[str]] = None
    ) -> Optional[Dict[str, float]]:
        if quotes:
            quotes_str = ",".join(quotes)
            url = f"{self.base_url}/rates?base={base}&quotes={quotes_str}"
        else:
            url = f"{self.base_url}/rates?base={base}"

        data = self.client.get(url)
        if data and isinstance(data, list):
            return {item["quote"]: item["rate"] for item in data if "quote" in item and "rate" in item}
        return None

    def get_rate(self, from_currency: str, to_currency: str) -> Optional[float]:
        url = f"{self.base_url}/rate/{from_currency}/{to_currency}"
        data = self.client.get(url)
        if data and "rate" in data:
            return data["rate"]
        return None

    def get_rate_to_usd(self, currency: str) -> Optional[float]:
        return self.get_rate(currency, "USD")

    def get_country_currency(self, country: str) -> Optional[str]:
        return COUNTRY_CURRENCY.get(country)

    def get_country_rate_to_usd(self, country: str) -> Optional[float]:
        currency = self.get_country_currency(country)
        if not currency:
            return None
        if currency == "USD":
            return 1.0
        return self.get_rate_to_usd(currency)
