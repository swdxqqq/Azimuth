from pathlib import Path
from typing import Dict, List, Optional

import pandas as pd
from pytz import country_timezones


COUNTRY_NAME_ALIASES = {
    "uae": "united arab emirates",
}

CAPITAL_TIMEZONE_OVERRIDES = {
    "AU": "Australia/Sydney",
    "BR": "America/Sao_Paulo",
    "CA": "America/Toronto",
    "RU": "Europe/Moscow",
    "UA": "Europe/Kyiv",
}


def _country_key(name: str) -> str:
    key = " ".join(name.strip().casefold().split())
    return COUNTRY_NAME_ALIASES.get(key, key)


def load_reference_data(data_dir: Path) -> tuple[Dict[str, int], Dict[str, List[str]]]:
    population_frame = pd.read_csv(data_dir / "population.csv")
    language_frame = pd.read_csv(data_dir / "languages.csv")

    populations = {
        _country_key(str(row.Country)): int(row.Population)
        for row in population_frame.itertuples()
        if pd.notna(row.Population)
    }
    languages = {
        _country_key(str(row.country)): [
            language.strip()
            for language in str(row.languages).split(";")
            if language.strip()
        ]
        for row in language_frame.itertuples()
        if pd.notna(row.languages)
    }
    return populations, languages


def get_country_reference(
    country_name: str,
    country_code: str,
    populations: Dict[str, int],
    languages: Dict[str, List[str]],
) -> dict:
    key = _country_key(country_name)
    timezones = country_timezones.get(country_code.upper(), [])

    return {
        "population": populations.get(key),
        "languages": languages.get(key),
        "timezone": CAPITAL_TIMEZONE_OVERRIDES.get(country_code.upper())
        or (timezones[0] if timezones else None),
    }
