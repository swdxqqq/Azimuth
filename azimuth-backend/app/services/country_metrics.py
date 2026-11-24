import math
from typing import Mapping, Optional


QUALITY_OF_LIFE_SOURCE = "Numbeo-inspired composite"


def calculate_quality_of_life(row: Mapping[str, object]) -> Optional[float]:
    """Calculate an adapted quality-of-life index from the available source metrics."""
    columns = {
        "purchasing_power": "Local Purchasing Power Index",
        "price_to_income": "Price To Income Ratio",
        "cost_of_living": "Cost of Living Index",
        "safety": "Safety Index",
        "healthcare": "Health Care Index",
        "commute_time": "Time Index(in minutes)",
        "pollution": "Pollution Index",
        "climate": "climate_comfort_score",
    }

    values = {}
    for key, column in columns.items():
        try:
            value = float(row[column])
        except (KeyError, TypeError, ValueError):
            return None
        if not math.isfinite(value):
            return None
        values[key] = value

    score = (
        100
        + values["purchasing_power"] / 2.5
        - values["price_to_income"]
        - values["cost_of_living"] / 10
        + values["safety"] / 2
        + values["healthcare"] / 2.5
        - values["commute_time"] / 2
        - values["pollution"] * 2 / 3
        + values["climate"] / 3
    )
    return round(max(0.0, score), 2)
