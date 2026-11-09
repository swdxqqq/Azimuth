import time
from typing import Any, Dict, Optional
from urllib.request import Request, urlopen
from urllib.error import URLError
import json


class HttpClient:
    def __init__(self, cache_ttl: int = 3600):
        self.cache_ttl = cache_ttl
        self._cache: Dict[str, tuple[float, Any]] = {}

    def get(self, url: str, timeout: int = 10) -> Optional[Any]:
        cache_key = url
        if cache_key in self._cache:
            cached_time, cached_data = self._cache[cache_key]
            if time.time() - cached_time < self.cache_ttl:
                return cached_data

        try:
            req = Request(url, headers={'User-Agent': 'Azimuth/1.0'})
            with urlopen(req, timeout=timeout) as response:
                data = json.loads(response.read().decode())
                self._cache[cache_key] = (time.time(), data)
                return data
        except (URLError, json.JSONDecodeError, KeyError):
            return None

    def clear_cache(self):
        self._cache.clear()
