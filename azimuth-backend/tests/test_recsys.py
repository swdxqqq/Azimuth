import pytest
from unittest.mock import patch, MagicMock
import pandas as pd
import numpy as np

from app.recsys.config import (
    CRITERIA,
    CriterionDirection,
    EngineConfig,
    UserPreference,
    TemperaturePreference,
)
from app.recsys.engine import RecSysEngine


@pytest.fixture
def mock_csv_data():
    countries = [f"Country_{i}" for i in range(20)]
    data = {"Country": countries}
    for key, criterion in CRITERIA.items():
        data[criterion.column] = np.random.uniform(10, 90, size=20)
    data["climate_avg_temp"] = np.random.uniform(5, 35, size=20)
    return pd.DataFrame(data)


@pytest.fixture
def engine(mock_csv_data):
    with patch.object(RecSysEngine, "_load_data", return_value=mock_csv_data):
        eng = RecSysEngine(year=2025)
        eng._load_data()
        yield eng


@pytest.fixture
def equal_prefs():
    return {
        key: UserPreference(priority=5, enabled=True, reliability=1.0)
        for key in CRITERIA
    }


class TestRecSysWeights:
    def test_equal_priorities_equal_weights(self, engine, equal_prefs):
        weights = engine.get_weights(equal_prefs)
        expected = 1.0 / len(CRITERIA)
        for w in weights.values():
            pytest.approx(w, expected, abs=1e-6)

    def test_weights_sum_to_one(self, engine, equal_prefs):
        weights = engine.get_weights(equal_prefs)
        pytest.approx(sum(weights.values()), 1.0, abs=1e-6)

    def test_disabled_criterion_excluded(self, engine, equal_prefs):
        equal_prefs["crime"].enabled = False
        weights = engine.get_weights(equal_prefs)
        assert "crime" not in weights
        assert len(weights) == len(CRITERIA) - 1

    def test_higher_priority_higher_weight(self, engine, equal_prefs):
        weights_low = engine.get_weights(equal_prefs)
        equal_prefs["safety"].priority = 10
        weights_high = engine.get_weights(equal_prefs)
        assert weights_high["safety"] > weights_low["safety"]

    def test_all_disabled_raises(self, engine):
        prefs = {k: UserPreference(enabled=False) for k in CRITERIA}
        with pytest.raises(ValueError):
            engine.get_weights(prefs)


class TestRecSysConfidence:
    def test_full_reliability_confidence(self, engine, equal_prefs):
        conf = engine.get_confidence(equal_prefs)
        pytest.approx(conf, 1.0, abs=1e-6)

    def test_partial_reliability(self, engine, equal_prefs):
        equal_prefs["crime"].reliability = 0.5
        conf = engine.get_confidence(equal_prefs)
        n = len(CRITERIA)
        expected = ((n - 1) * 1.0 + 0.5) / n
        pytest.approx(conf, expected, abs=1e-6)


class TestRecSysRecommend:
    def test_recommend_returns_top_n(self, engine, equal_prefs):
        results = engine.recommend(equal_prefs, top_n=5)
        assert len(results) == 5

    def test_recommend_sorted_descending(self, engine, equal_prefs):
        results = engine.recommend(equal_prefs, top_n=10)
        scores = results["Final_Score"].values
        assert all(scores[i] >= scores[i + 1] for i in range(len(scores) - 1))

    def test_recommend_with_temperature(self, engine, equal_prefs):
        temp = TemperaturePreference(enabled=True, target_temp=22.0)
        results = engine.recommend(equal_prefs, temp_pref=temp, top_n=5)
        assert len(results) == 5
        assert "Temperature_Fit" in results.columns

    def test_recommend_temperature_disabled(self, engine, equal_prefs):
        temp = TemperaturePreference(enabled=False)
        results = engine.recommend(equal_prefs, temp_pref=temp, top_n=5)
        assert len(results) == 5

    def test_recommend_scores_positive(self, engine, equal_prefs):
        results = engine.recommend(equal_prefs, top_n=10)
        assert all(results["Final_Score"] > 0)

    def test_recommend_with_imputation(self, engine, equal_prefs):
        equal_prefs["internet_speed"].reliability = 0.3
        results = engine.recommend(equal_prefs, top_n=5)
        assert len(results) == 5

    def test_recommend_custom_top_n(self, engine, equal_prefs):
        results = engine.recommend(equal_prefs, top_n=3)
        assert len(results) == 3

    def test_recommend_includes_confidence(self, engine, equal_prefs):
        results = engine.recommend(equal_prefs, top_n=5)
        assert "Confidence" in results.columns
        conf_value = results["Confidence"].iloc[0]
        pytest.approx(conf_value, 1.0, abs=1e-6)


class TestEngineConfig:
    def test_default_config(self):
        cfg = EngineConfig()
        assert cfg.priority_exponent == 1.3
        assert cfg.epsilon == 0.02
        assert cfg.temperature_weight == 0.08

    def test_custom_config(self):
        cfg = EngineConfig(priority_exponent=2.0, epsilon=0.01)
        assert cfg.priority_exponent == 2.0
        assert cfg.epsilon == 0.01


class TestNormalize:
    def test_higher_is_better_normalized(self, engine, mock_csv_data):
        prefs = {"safety": UserPreference(priority=5, enabled=True, reliability=1.0)}
        keys = ["safety"]
        df_norm = engine._normalize(mock_csv_data, keys)
        col = CRITERIA["safety"].column
        assert df_norm[col].min() >= 0.0
        assert df_norm[col].max() <= 1.0

    def test_lower_is_better_inverted(self, engine, mock_csv_data):
        prefs = {"crime": UserPreference(priority=5, enabled=True, reliability=1.0)}
        keys = ["crime"]
        df_norm = engine._normalize(mock_csv_data, keys)
        col = CRITERIA["crime"].column
        assert df_norm[col].min() >= 0.0
        assert df_norm[col].max() <= 1.0
