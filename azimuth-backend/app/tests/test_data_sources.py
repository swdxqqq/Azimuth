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
from app.constants import COUNTRY_CURRENCY
from app.services.country_metrics import calculate_quality_of_life
from app.services.country_reference import (
    get_country_reference,
    get_hdi_value,
    load_hdi_data,
    load_reference_data,
)


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


class TestHdiData(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.data_dir = Path(__file__).resolve().parents[2] / "data"
        cls.hdi_values = load_hdi_data(cls.data_dir / "hdi.csv")

    def test_country_aliases_resolve_to_dataset_rows(self):
        self.assertEqual(
            get_hdi_value("Czech Republic", self.hdi_values),
            get_hdi_value("Czechia", self.hdi_values),
        )
        self.assertEqual(
            get_hdi_value("Hong Kong (China)", self.hdi_values),
            get_hdi_value("Hong Kong", self.hdi_values),
        )
        self.assertEqual(
            get_hdi_value("Bosnia And Herzegovina", self.hdi_values),
            get_hdi_value("Bosnia and Herzegovina", self.hdi_values),
        )

    def test_catalog_coverage_reports_only_countries_without_hdi(self):
        catalog = pd.read_csv(self.data_dir / "merged_all_features_2025.csv")
        missing = sorted(
            country
            for country in catalog["Country"].dropna().astype(str)
            if get_hdi_value(country, self.hdi_values) is None
        )

        self.assertEqual(missing, ["Taiwan"])

    def test_rejects_duplicate_names_after_alias_normalization(self):
        with tempfile.TemporaryDirectory() as directory:
            hdi_file = Path(directory) / "hdi.csv"
            pd.DataFrame({
                "Country": ["Czechia", "Czech Republic"],
                "HDI": [0.915, 0.915],
            }).to_csv(hdi_file, index=False)

            with self.assertRaisesRegex(ValueError, "Duplicate HDI country"):
                load_hdi_data(hdi_file)


class TestCountryCompositeMetrics(unittest.TestCase):
    def test_adapted_quality_of_life_formula_uses_all_source_inputs(self):
        row = {
            "Local Purchasing Power Index": 100.0,
            "Price To Income Ratio": 10.0,
            "Cost of Living Index": 50.0,
            "Safety Index": 60.0,
            "Health Care Index": 70.0,
            "Time Index(in minutes)": 30.0,
            "Pollution Index": 20.0,
            "climate_comfort_score": 80.0,
        }

        self.assertEqual(calculate_quality_of_life(row), 181.33)

    def test_quality_of_life_is_missing_when_an_input_is_unavailable(self):
        self.assertIsNone(calculate_quality_of_life({}))

    def test_seeded_countries_have_currency_mappings(self):
        data_file = Path(__file__).resolve().parents[2] / "data" / "merged_all_features_2025.csv"
        countries = pd.read_csv(data_file)["Country"].dropna().astype(str)
        missing = sorted(set(countries) - set(COUNTRY_CURRENCY))

        self.assertEqual(missing, [])


if __name__ == "__main__":
    unittest.main()
