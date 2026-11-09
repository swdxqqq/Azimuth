from pathlib import Path
from typing import Dict, List, Optional

import numpy as np
import pandas as pd

from .config import (
    CRITERIA,
    CriterionDirection,
    EngineConfig,
    TemperaturePreference,
    UserPreference,
    TEMPERATURE_COLUMN,
)


class RecSysEngine:
    def __init__(self, year: int = 2025, config: Optional[EngineConfig] = None):
        self.year = year
        self.config = config or EngineConfig()
        self._data_dir = Path(__file__).parent.parent.parent / "data"
        self._raw_df: Optional[pd.DataFrame] = None

    def _load_data(self) -> pd.DataFrame:
        if self._raw_df is None:
            self._raw_df = pd.read_csv(
                self._data_dir / f"merged_all_features_{self.year}.csv"
            )
        return self._raw_df

    @staticmethod
    def _active_keys(prefs: Dict[str, UserPreference]) -> List[str]:
        keys = [k for k, p in prefs.items() if p.enabled]
        if not keys:
            raise ValueError("At least one criterion must be enabled")
        return keys

    def _impute(
        self, df: pd.DataFrame, prefs: Dict[str, UserPreference], keys: List[str]
    ) -> pd.DataFrame:
        df = df.copy()
        for key in keys:
            col = CRITERIA[key].column
            q = prefs[key].reliability
            median = df[col].median()
            df[col] = (q * df[col] + (1 - q) * median).fillna(median)
        return df

    def _normalize(self, df: pd.DataFrame, keys: List[str]) -> pd.DataFrame:
        df = df.copy()
        for key in keys:
            col = CRITERIA[key].column
            lo, hi = df[col].min(), df[col].max()
            if hi == lo:
                df[col] = 0.5
                continue
            val = (df[col] - lo) / (hi - lo)
            if CRITERIA[key].direction == CriterionDirection.LOWER_IS_BETTER:
                val = 1.0 - val
            df[col] = val
        return df

    def _weights(
        self, prefs: Dict[str, UserPreference], keys: List[str]
    ) -> Dict[str, float]:
        raw = {k: prefs[k].priority ** self.config.priority_exponent for k in keys}
        total = sum(raw.values())
        return {k: v / total for k, v in raw.items()}

    def _core_scores(
        self, df: pd.DataFrame, weights: Dict[str, float]
    ) -> pd.Series:
        eps = self.config.epsilon
        terms = sum(
            w * np.log(np.maximum(df[CRITERIA[k].column], eps))
            for k, w in weights.items()
        )
        return np.exp(terms)

    def _temperature_fit(
        self, df: pd.DataFrame, temp: TemperaturePreference
    ) -> pd.Series:
        if not temp.enabled or TEMPERATURE_COLUMN not in df.columns:
            return pd.Series(1.0, index=df.index)
        d = np.maximum(
            0, np.abs(df[TEMPERATURE_COLUMN] - temp.target_temp) - self.config.temperature_tolerance
        )
        return np.exp(-0.5 * (d / self.config.temperature_scale) ** 2)

    def get_weights(self, prefs: Dict[str, UserPreference]) -> Dict[str, float]:
        return self._weights(prefs, self._active_keys(prefs))

    def get_confidence(self, prefs: Dict[str, UserPreference]) -> float:
        keys = self._active_keys(prefs)
        w = self._weights(prefs, keys)
        return sum(w[k] * prefs[k].reliability for k in keys)

    def recommend(
        self,
        prefs: Dict[str, UserPreference],
        temp_pref: Optional[TemperaturePreference] = None,
        top_n: int = 10,
    ) -> pd.DataFrame:
        keys = self._active_keys(prefs)
        df = self._impute(self._load_data(), prefs, keys)
        df_norm = self._normalize(df, keys)
        w = self._weights(prefs, keys)
        core = self._core_scores(df_norm, w)

        tp = temp_pref or TemperaturePreference()
        t_fit = self._temperature_fit(df, tp)
        lam = self.config.temperature_weight
        final = (core ** (1 - lam) * t_fit ** lam) if tp.enabled else core

        results = pd.DataFrame({
            "Country": self._load_data()["Country"],
            "Final_Score": final,
            "Core_Score": core,
            "Temperature_Fit": t_fit,
        })
        results = results.sort_values("Final_Score", ascending=False).head(top_n)
        results = results.reset_index(drop=True)
        results.index += 1
        results["Confidence"] = self.get_confidence(prefs)
        return results
