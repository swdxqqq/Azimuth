import tempfile
import unittest
from pathlib import Path

import pandas as pd

from app.data_collection.collect_climate import (
    average_monthly_comfort_score,
    compute_climate_features,
    normalize_country_name,
    score_monthly_temperature,
)
from app.services.country_reference import get_country_reference, load_reference_data


class TestClimateComfortScore(unittest.TestCase):
    def test_score_is_smooth_outside_comfort_band(self):
        cases = {
            15.0: 100.0,
            20.0: 100.0,
            25.0: 100.0,
            14.0: 90.0,
            26.0: 90.0,
            10.0: 50.0,
            30.0: 50.0,
            0.0: 0.0,
            40.0: 0.0,
        }
        for monthly_mean, expected in cases.items():
            with self.subTest(monthly_mean=monthly_mean):
                self.assertEqual(score_monthly_temperature(monthly_mean), expected)

    def test_annual_score_averages_monthly_comfort_scores(self):
        monthly_means = [20.0, 14.0, 26.0] + [20.0] * 9
        self.assertEqual(average_monthly_comfort_score(monthly_means), 98.33)

    def test_daily_temperatures_are_grouped_by_calendar_month(self):
        daily_data = pd.DataFrame({
            "date": ["2020-01-01", "2021-01-01", "2020-02-01"],
            "mean_temp": [20.0, 24.0, 30.0],
        })

        features = compute_climate_features(daily_data)

        self.assertEqual(features["climate_comfort_score"], 75.0)
        self.assertEqual(features["climate_avg_temp"], 26.0)
        self.assertEqual(features["climate_seasonality"], 8.0)

    def test_country_names_match_seed_dataset(self):
        self.assertEqual(normalize_country_name("Bosnia and Herzegovina"), "Bosnia And Herzegovina")
        self.assertEqual(normalize_country_name("Hong Kong"), "Hong Kong (China)")


class TestCountryReferenceData(unittest.TestCase):
    def test_population_and_languages_support_uae_alias(self):
        with tempfile.TemporaryDirectory() as directory:
            data_dir = Path(directory)
            pd.DataFrame({
                "Country": ["United Arab Emirates"],
                "Population": [9890400],
            }).to_csv(data_dir / "population.csv", index=False)
            pd.DataFrame({
                "country": ["United Arab Emirates"],
                "languages": ["Arabic; English"],
            }).to_csv(data_dir / "languages.csv", index=False)

            populations, languages = load_reference_data(data_dir)
            reference = get_country_reference("UAE", "AE", populations, languages)

        self.assertEqual(reference["population"], 9890400)
        self.assertEqual(reference["languages"], ["Arabic", "English"])
        self.assertEqual(reference["timezone"], "Asia/Dubai")


if __name__ == "__main__":
    unittest.main()
