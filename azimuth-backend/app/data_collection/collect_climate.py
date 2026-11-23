from pathlib import Path
import math
import time
import pandas as pd
import requests

try:
    from .capital_coordinates import CAPITAL_COORDINATES
except ImportError:
    from capital_coordinates import CAPITAL_COORDINATES


DATA_DIR = Path(__file__).parent.parent.parent / "data"
OUTPUT_FILE = DATA_DIR / "climate_features.csv"
COMFORT_MIN_C = 15.0
COMFORT_MAX_C = 25.0
PENALTY_PER_DEGREE = 10.0
REQUEST_BATCH_SIZE = 10
INTER_BATCH_DELAY_SECONDS = 3
RATE_LIMIT_RETRY_DELAYS = (10, 30, 60)

CLIMATE_COUNTRY_ALIASES = {
    "bosnia and herzegovina": "Bosnia And Herzegovina",
    "hong kong": "Hong Kong (China)",
}


def normalize_country_name(country_name: str) -> str:
    normalized = country_name.strip()
    return CLIMATE_COUNTRY_ALIASES.get(normalized.casefold(), normalized)


def fetch_temperature_data(lat: float, lon: float) -> pd.DataFrame:
    return fetch_temperature_batch([(lat, lon)])[0]


def fetch_temperature_batch(locations: list[tuple[float, float]]) -> list[pd.DataFrame]:
    if not locations:
        return []

    url = "https://archive-api.open-meteo.com/v1/archive"
    params = {
        "latitude": ",".join(str(lat) for lat, _ in locations),
        "longitude": ",".join(str(lon) for _, lon in locations),
        "start_date": "2020-01-01",
        "end_date": "2024-12-31",
        "daily": "temperature_2m_max,temperature_2m_min",
        "timezone": "auto",
    }

    response = requests.get(url, params=params, timeout=60)
    for default_delay in RATE_LIMIT_RETRY_DELAYS:
        if response.status_code != 429:
            break

        retry_delay = default_delay
        retry_after = response.headers.get("Retry-After")
        if retry_after:
            try:
                retry_delay = min(120, max(1, int(float(retry_after))))
            except ValueError:
                pass

        print(f"  Open-Meteo rate limit; retrying in {retry_delay}s...")
        time.sleep(retry_delay)
        response = requests.get(url, params=params, timeout=60)
    response.raise_for_status()
    payload = response.json()
    responses = payload if isinstance(payload, list) else [payload]
    if len(responses) != len(locations):
        raise ValueError("Open-Meteo returned a different number of locations than requested")

    data_frames = []
    for data in responses:
        daily = data["daily"]
        frame = pd.DataFrame({
            "date": daily["time"],
            "temp_max": daily["temperature_2m_max"],
            "temp_min": daily["temperature_2m_min"],
            "mean_temp": [
                (maximum + minimum) / 2
                if maximum is not None and minimum is not None
                else float("nan")
                for maximum, minimum in zip(
                    daily["temperature_2m_max"],
                    daily["temperature_2m_min"],
                )
            ],
        })
        data_frames.append(frame)

    return data_frames


def score_monthly_temperature(monthly_mean_c: float) -> float:
    """Score a monthly mean from 0–100, with a smooth penalty outside 15–25°C."""
    distance_from_comfort_band = max(
        COMFORT_MIN_C - monthly_mean_c,
        0.0,
        monthly_mean_c - COMFORT_MAX_C,
    )
    return max(0.0, 100.0 - distance_from_comfort_band * PENALTY_PER_DEGREE)


def average_monthly_comfort_score(monthly_means: list[float]) -> float:
    scores = [
        score_monthly_temperature(temperature)
        for temperature in monthly_means
        if math.isfinite(temperature)
    ]
    if not scores:
        raise ValueError("Climate dataset does not contain monthly temperature values")
    return round(sum(scores) / len(scores), 2)


def compute_climate_features(daily_df: pd.DataFrame) -> dict:
    if daily_df.empty:
        raise ValueError("Cannot compute climate features from an empty dataset")

    monthly_values: dict[int, list[float]] = {}
    for date, value in daily_df[["date", "mean_temp"]].itertuples(index=False, name=None):
        if pd.isna(value) or not math.isfinite(float(value)):
            continue
        month = int(str(date)[5:7])
        monthly_values.setdefault(month, []).append(float(value))

    monthly_avg = [
        sum(values) / len(values)
        for _, values in sorted(monthly_values.items())
        if values
    ]
    if not monthly_avg:
        raise ValueError("Climate dataset does not contain monthly temperature values")
    comfort_score = average_monthly_comfort_score(monthly_avg)

    return {
        "climate_comfort_score": comfort_score,
        "climate_avg_temp": round(sum(monthly_avg) / len(monthly_avg), 2),
        "climate_seasonality": round(max(monthly_avg) - min(monthly_avg), 2)
    }


def collect_all_climate_data():
    results = []
    countries = list(CAPITAL_COORDINATES.items())
    known_names = {normalize_country_name(country) for country, _ in countries}

    if OUTPUT_FILE.exists():
        existing = pd.read_csv(OUTPUT_FILE)
        if "Country" in existing and len(existing) < len(countries):
            existing_names = {
                normalize_country_name(str(country))
                for country in existing["Country"].dropna()
            }
            if existing_names and existing_names.issubset(known_names):
                results = existing.to_dict("records")

    completed = {normalize_country_name(row["Country"]) for row in results}
    pending = [
        (country, coordinates)
        for country, coordinates in countries
        if normalize_country_name(country) not in completed
    ]

    for start in range(0, len(pending), REQUEST_BATCH_SIZE):
        batch = pending[start:start + REQUEST_BATCH_SIZE]
        temperatures = fetch_temperature_batch([coordinates for _, coordinates in batch])

        for offset, ((country, _), daily_df) in enumerate(zip(batch, temperatures), 1):
            country_name = normalize_country_name(country)
            features = compute_climate_features(daily_df)
            features["Country"] = country_name
            results.append(features)

            index = len(completed) + start + offset
            print(
                f"[{index}/{len(countries)}] {country_name}: "
                f"comfort={features['climate_comfort_score']:.1f}/100, "
                f"avg={features['climate_avg_temp']:.1f}°C"
            )

        pd.DataFrame(results).to_csv(OUTPUT_FILE, index=False)
        print(f"  [Saved: {len(results)} countries]")
        if start + REQUEST_BATCH_SIZE < len(pending):
            time.sleep(INTER_BATCH_DELAY_SECONDS)

    pd.DataFrame(results).to_csv(OUTPUT_FILE, index=False)
    return pd.DataFrame(results)


def main():
    print(f"Collecting climate data for {len(CAPITAL_COORDINATES)} countries...")
    result_df = collect_all_climate_data()
    print(f"\nDone! Saved {len(result_df)} countries to {OUTPUT_FILE.name}")
    print(result_df.head())


if __name__ == "__main__":
    main()
