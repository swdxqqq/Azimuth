import math
from pathlib import Path
from typing import Dict, List, Optional

import pandas as pd
from pytz import country_timezones


COUNTRY_NAME_ALIASES = {
    "uae": "united arab emirates",
}

HDI_NAME_ALIASES = {
    "czech republic": "czechia",
    "hong kong (china)": "hong kong",
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


def _hdi_key(name: str) -> str:
    key = " ".join(name.strip().casefold().split())
    return HDI_NAME_ALIASES.get(key, key)


def load_hdi_data(data_file: Path) -> Dict[str, float]:
    frame = pd.read_csv(data_file)
    required_columns = {"Country", "HDI"}
    if not required_columns.issubset(frame.columns):
        raise ValueError("HDI CSV must contain Country and HDI columns")

    hdi_values: Dict[str, float] = {}
    for row in frame.itertuples(index=False):
        raw_country_name = getattr(row, "Country")
        raw_value = getattr(row, "HDI")
        if pd.isna(raw_country_name) or pd.isna(raw_value):
            raise ValueError("HDI CSV contains a missing country name or value")
        country_name = str(raw_country_name).strip()
        if not country_name:
            raise ValueError("HDI CSV contains a missing country name or value")

        value = float(raw_value)
        if not math.isfinite(value) or not 0.0 <= value <= 1.0:
            raise ValueError(f"HDI for {country_name} must be between 0 and 1")

        key = _hdi_key(country_name)
        if key in hdi_values:
            raise ValueError(f"Duplicate HDI country after name normalization: {country_name}")
        hdi_values[key] = value

    return hdi_values


def get_hdi_value(country_name: str, hdi_values: Dict[str, float]) -> Optional[float]:
    return hdi_values.get(_hdi_key(country_name))


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
