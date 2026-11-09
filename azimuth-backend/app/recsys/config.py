from dataclasses import dataclass
from enum import Enum
from typing import Dict, Optional


class CriterionDirection(Enum):
    LOWER_IS_BETTER = "lower"
    HIGHER_IS_BETTER = "higher"


@dataclass(frozen=True)
class Criterion:
    name: str
    column: str
    direction: CriterionDirection
    group: str


@dataclass
class UserPreference:
    priority: int = 5
    enabled: bool = True
    reliability: float = 1.0


@dataclass
class TemperaturePreference:
    enabled: bool = False
    target_temp: float = 20.0


@dataclass(frozen=True)
class EngineConfig:
    priority_exponent: float = 1.3
    epsilon: float = 0.02
    temperature_weight: float = 0.08
    temperature_tolerance: float = 2.0
    temperature_scale: float = 5.0


CRITERIA: Dict[str, Criterion] = {
    "crime": Criterion(
        name="Crime Index",
        column="crime",
        direction=CriterionDirection.LOWER_IS_BETTER,
        group="Safety & Health"
    ),
    "safety": Criterion(
        name="Safety Index",
        column="safety",
        direction=CriterionDirection.HIGHER_IS_BETTER,
        group="Safety & Health"
    ),
    "healthcare": Criterion(
        name="Health Care Index",
        column="healthcare",
        direction=CriterionDirection.HIGHER_IS_BETTER,
        group="Safety & Health"
    ),
    "pollution": Criterion(
        name="Pollution Index",
        column="pollution",
        direction=CriterionDirection.LOWER_IS_BETTER,
        group="Safety & Health"
    ),
    "cost_of_living": Criterion(
        name="Cost of Living Index",
        column="cost_of_living",
        direction=CriterionDirection.LOWER_IS_BETTER,
        group="Cost & Economy"
    ),
    "purchasing_power": Criterion(
        name="Local Purchasing Power Index",
        column="purchasing_power",
        direction=CriterionDirection.HIGHER_IS_BETTER,
        group="Cost & Economy"
    ),
    "property_price_to_income": Criterion(
        name="Price to Income Ratio",
        column="property_price_to_income",
        direction=CriterionDirection.LOWER_IS_BETTER,
        group="Cost & Economy"
    ),
    "climate": Criterion(
        name="Climate Comfort Score",
        column="climate",
        direction=CriterionDirection.HIGHER_IS_BETTER,
        group="Climate"
    ),
    "internet_speed": Criterion(
        name="Internet Speed",
        column="internet_speed",
        direction=CriterionDirection.HIGHER_IS_BETTER,
        group="Digital & Infrastructure"
    ),
    "traffic": Criterion(
        name="Traffic Index",
        column="traffic",
        direction=CriterionDirection.LOWER_IS_BETTER,
        group="Digital & Infrastructure"
    ),
    "visa_score": Criterion(
        name="Visa Score",
        column="visa_score",
        direction=CriterionDirection.HIGHER_IS_BETTER,
        group="Visa & Accessibility"
    ),
}

TEMPERATURE_COLUMN = "climate_avg_temp"
