from pathlib import Path
import pandas as pd
import requests

from capital_coordinates import CAPITAL_COORDINATES


DATA_DIR = Path(__file__).parent.parent.parent / "data"
OUTPUT_FILE = DATA_DIR / "climate_features.csv"


def fetch_temperature_data(lat: float, lon: float) -> pd.DataFrame:
    url = "https://archive-api.open-meteo.com/v1/archive"
    params = {
        "latitude": lat,
        "longitude": lon,
        "start_date": "2020-01-01",
        "end_date": "2024-12-31",
        "daily": "temperature_2m_max,temperature_2m_min",
        "timezone": "auto"
    }
    
    response = requests.get(url, params=params, timeout=30)
    data = response.json()
    
    df = pd.DataFrame({
        "date": pd.to_datetime(data["daily"]["time"]),
        "temp_max": data["daily"]["temperature_2m_max"],
        "temp_min": data["daily"]["temperature_2m_min"]
    })
    df["mean_temp"] = (df["temp_max"] + df["temp_min"]) / 2
    
    return df


def compute_climate_features(daily_df: pd.DataFrame) -> dict:
    daily_df["year"] = daily_df["date"].dt.year
    daily_df["month"] = daily_df["date"].dt.month
    
    monthly_avg = daily_df.groupby("month")["mean_temp"].mean()
    
    comfort_months = ((monthly_avg >= 15) & (monthly_avg <= 25)).sum()
    comfort_score = (comfort_months / 12) * 100
    
    return {
        "climate_comfort_score": round(comfort_score, 2),
        "climate_avg_temp": round(monthly_avg.mean(), 2),
        "climate_seasonality": round(monthly_avg.max() - monthly_avg.min(), 2)
    }


def collect_all_climate_data():
    results = []
    
    for idx, (country, (lat, lon)) in enumerate(CAPITAL_COORDINATES.items(), 1):
        print(f"[{idx}/{len(CAPITAL_COORDINATES)}] {country}...", end=" ")
        
        daily_df = fetch_temperature_data(lat, lon)
        features = compute_climate_features(daily_df)
        features["Country"] = country
        results.append(features)
        
        print(f"OK (comfort={features['climate_comfort_score']:.0f}%, avg={features['climate_avg_temp']:.1f}C)")
        
        if idx % 10 == 0:
            pd.DataFrame(results).to_csv(OUTPUT_FILE, index=False)
            print(f"  [Saved: {idx} countries]")
        
    
    pd.DataFrame(results).to_csv(OUTPUT_FILE, index=False)
    return pd.DataFrame(results)


def main():
    print(f"Collecting climate data for {len(CAPITAL_COORDINATES)} countries...")
    result_df = collect_all_climate_data()
    print(f"\nDone! Saved {len(result_df)} countries to {OUTPUT_FILE.name}")
    print(result_df.head())


if __name__ == "__main__":
    main()
