import unittest
from datetime import datetime, timezone
from types import SimpleNamespace
from unittest.mock import patch

import pandas as pd

from app.clients.imf_client import IMFClient
from app.services.economy_service import EconomyService


class TestImfEconomyHistory(unittest.TestCase):
    @patch("app.clients.imf_client.imf_get")
    def test_returns_five_historical_years_and_ignores_forecasts(self, imf_get):
        rows = []
        for year in range(2020, 2026):
            for indicator, value in (
                ("NGDPDPC", 10000 + year),
                ("PCPIPCH", year - 2019),
                ("LUR", 8 - (year - 2020) / 10),
            ):
                rows.append({
                    "TIME_PERIOD": str(year),
                    "INDICATOR": indicator,
                    "OBS_VALUE": value,
                })
        imf_get.return_value = pd.DataFrame(rows)

        response = IMFClient().get_country_economy("Switzerland")

        self.assertIsNotNone(response)
        self.assertEqual(response["year"], 2024)
        self.assertEqual([point["year"] for point in response["history"]], list(range(2020, 2025)))
        self.assertEqual(response["history"][-1]["gdp_per_capita"], 12024.0)
        self.assertNotIn(2025, [point["year"] for point in response["history"]])


class TestEconomyHistoryCache(unittest.TestCase):
    class Query:
        def __init__(self, rows=None):
            self.rows = rows or []
            self.deleted = False

        def filter(self, *_conditions):
            return self

        def all(self):
            return self.rows

        def delete(self, **_kwargs):
            self.deleted = True

    class Database:
        def __init__(self, rows=None):
            self.query_result = TestEconomyHistoryCache.Query(rows)
            self.added = []
            self.commits = 0

        def query(self, _model):
            return self.query_result

        def add(self, row):
            self.added.append(row)

        def commit(self):
            self.commits += 1

    def test_saves_all_years_without_keeping_stale_forecast_rows(self):
        database = self.Database([
            SimpleNamespace(year=2025, indicator_type="inflation", value=99.0)
        ])
        service = EconomyService(database)
        history = [
            {
                "year": year,
                "gdp_per_capita": float(year),
                "inflation": 2.0,
                "unemployment": 4.0,
            }
            for year in range(2020, 2025)
        ]

        service._save_to_cache(7, {"history": history})

        self.assertTrue(database.query_result.deleted)
        self.assertEqual(len(database.added), 15)
        self.assertEqual({row.year for row in database.added}, set(range(2020, 2025)))
        self.assertEqual(database.commits, 1)

    def test_forecast_only_response_does_not_delete_cached_history(self):
        database = self.Database()
        service = EconomyService(database)

        service._save_to_cache(7, {
            "history": [{
                "year": 2025,
                "gdp_per_capita": 100.0,
                "inflation": 2.0,
                "unemployment": 4.0,
            }]
        })

        self.assertFalse(database.query_result.deleted)
        self.assertEqual(database.added, [])
        self.assertEqual(database.commits, 0)

    def test_cache_groups_indicators_by_year_and_requires_history(self):
        updated_at = datetime.now(timezone.utc)
        rows = [
            SimpleNamespace(
                country_id=7,
                year=year,
                indicator_type=indicator,
                value=value,
                updated_at=updated_at,
            )
            for year in (2020, 2024, 2025)
            for indicator, value in (
                ("gdp_per_capita", year * 100.0),
                ("inflation", 2.0),
                ("unemployment", 4.0),
            )
        ]
        service = EconomyService(self.Database(rows))

        response = service._get_from_cache(7)

        self.assertEqual(response["year"], 2024)
        self.assertEqual([point["year"] for point in response["history"]], [2020, 2024])
        self.assertEqual(response["gdp_per_capita"], 2024 * 100.0)

        legacy_service = EconomyService(self.Database([
            SimpleNamespace(
                country_id=7,
                year=2025,
                indicator_type="gdp_per_capita",
                value=100.0,
                updated_at=updated_at,
            )
        ]))
        self.assertIsNone(legacy_service._get_from_cache(7))


if __name__ == "__main__":
    unittest.main()
