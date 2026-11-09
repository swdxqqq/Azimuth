import unittest

import numpy as np
import pandas as pd

from .config import CRITERIA, EngineConfig, TemperaturePreference, UserPreference
from .engine import RecSysEngine


class TestRecSysEngine(unittest.TestCase):
    def setUp(self):
        self.engine = RecSysEngine(year=2025)

    def _make_equal_prefs(self, enabled_keys=None):
        if enabled_keys is None:
            enabled_keys = list(CRITERIA.keys())
        return {
            key: UserPreference(priority=1, enabled=(key in enabled_keys), reliability=1.0)
            for key in CRITERIA.keys()
        }

    def test_01_equal_priorities_give_equal_weights(self):
        prefs = self._make_equal_prefs()
        weights = self.engine.get_weights(prefs)
        expected = 1.0 / len(prefs)
        for key, weight in weights.items():
            self.assertAlmostEqual(weight, expected, places=6)

    def test_02_disable_one_criterion_removes_weight(self):
        prefs_all = self._make_equal_prefs()
        weights_all = self.engine.get_weights(prefs_all)

        prefs_disabled = self._make_equal_prefs()
        prefs_disabled["crime"].enabled = False
        weights_disabled = self.engine.get_weights(prefs_disabled)

        self.assertNotIn("crime", weights_disabled)
        self.assertEqual(len(weights_disabled), len(CRITERIA) - 1)

        expected = 1.0 / len(weights_disabled)
        for weight in weights_disabled.values():
            self.assertAlmostEqual(weight, expected, places=6)

    def test_03_disable_both_climate_no_temperature(self):
        prefs = self._make_equal_prefs()
        prefs["climate_comfort"].enabled = False
        prefs["climate_seasonality"].enabled = False

        temp_pref = TemperaturePreference(enabled=False)

        weights = self.engine.get_weights(prefs)
        self.assertNotIn("climate_comfort", weights)
        self.assertNotIn("climate_seasonality", weights)

        results = self.engine.recommend(prefs, temp_pref, top_n=5)
        self.assertEqual(len(results), 5)

    def test_04_disable_criteria_within_group_independently(self):
        prefs = self._make_equal_prefs()
        prefs["crime"].enabled = False

        weights = self.engine.get_weights(prefs)

        self.assertNotIn("crime", weights)
        self.assertIn("health_care", weights)
        self.assertIn("pollution", weights)

    def test_05_reenable_criterion_restores_priority(self):
        prefs = self._make_equal_prefs()
        prefs["crime"].enabled = False
        weights_disabled = self.engine.get_weights(prefs)

        prefs["crime"].enabled = True
        weights_enabled = self.engine.get_weights(prefs)

        self.assertIn("crime", weights_enabled)
        self.assertEqual(len(weights_enabled), len(CRITERIA))

        expected = 1.0 / len(CRITERIA)
        for weight in weights_enabled.values():
            self.assertAlmostEqual(weight, expected, places=6)

    def test_06_increase_priority_increases_weight(self):
        prefs = self._make_equal_prefs()
        weights_low = self.engine.get_weights(prefs)

        prefs["crime"].priority = 10
        weights_high = self.engine.get_weights(prefs)

        self.assertGreater(weights_high["crime"], weights_low["crime"])

    def test_07_temperature_off_no_influence(self):
        prefs = self._make_equal_prefs()

        temp_off = TemperaturePreference(enabled=False)
        results_off = self.engine.recommend(prefs, temp_off, top_n=5)

        temp_on = TemperaturePreference(enabled=True, target_temp=20.0)
        results_on = self.engine.recommend(prefs, temp_on, top_n=5)

        for i in range(len(results_off)):
            self.assertNotEqual(
                results_off.iloc[i]["Final_Score"],
                results_on.iloc[i]["Final_Score"]
            )

    def test_08_all_disabled_raises_error(self):
        prefs = {
            key: UserPreference(priority=1, enabled=False, reliability=1.0)
            for key in CRITERIA.keys()
        }

        with self.assertRaises(ValueError):
            self.engine.recommend(prefs)

    def test_09_missing_data_not_zeroed(self):
        prefs = self._make_equal_prefs()
        prefs["internet_speed"].reliability = 0.7

        results = self.engine.recommend(prefs, top_n=5)
        self.assertEqual(len(results), 5)

        for score in results["Final_Score"]:
            self.assertGreater(score, 0)

    def test_10_same_weights_for_all_countries(self):
        prefs = self._make_equal_prefs()
        prefs["internet_speed"].reliability = 0.5

        weights = self.engine.get_weights(prefs)
        confidence = self.engine.get_confidence(prefs)

        n = len(prefs)
        expected_confidence = ((n - 1) / n) * 1.0 + (1 / n) * 0.5
        self.assertAlmostEqual(confidence, expected_confidence, places=6)

        results = self.engine.recommend(prefs, top_n=10)
        self.assertEqual(len(results), 10)


if __name__ == "__main__":
    unittest.main()
