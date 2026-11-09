from functools import reduce
from typing import Set, Tuple
from sklearn.preprocessing import StandardScaler

import pandas as pd

import load_data as ld

EXCLUDE_COLS = ['Country', 'Year']


def get_intersection_set(dfs: Tuple[pd.DataFrame, ...]) -> Set[str]:
    """Find countries present in all provided DataFrames"""
    sets = [set(df["Country"]) for df in dfs]
    return reduce(lambda left, right: left.intersection(right), sets)


def intersection_all_count() -> None:
    """Print the number of countries present in all years"""
    dfs = ld.results_by_year()
    result = get_intersection_set(dfs)
    print(f"Intersection of all years: {len(result)} rows")


def intersection_all() -> pd.DataFrame:
    """Filter all yearly DataFrames to keep only countries present in every year,
    then concatenate into a single DataFrame"""
    dfs = ld.results_by_year()
    common_countries = get_intersection_set(dfs)
    
    intersected_dfs = [df[df["Country"].isin(common_countries)].copy() for df in dfs]
    
    return pd.concat(intersected_dfs, ignore_index=True)


def validate_data(df: pd.DataFrame) -> None:
    """Print validation info about the DataFrame: structure, stats, nulls, unique countries"""
    print(f"Combined DataFrame Info:\n{df.info()}\n")
    print(f"Description:\n{df.describe()}\n")
    print(f"Null values:\n{df.isnull().sum()}\n")
    print(f"Number of unique countries: {df['Country'].nunique()}")


def normalize_data(df: pd.DataFrame, method: str = "minmax") -> pd.DataFrame:
    """Apply Min-Max normalization to all numeric columns except Country and Year"""
    numeric_cols = [
        col for col in df.select_dtypes(include=['number']).columns
        if col not in EXCLUDE_COLS
    ]
    if method == "standard":
        normalized_df = df.copy()
        scaler = StandardScaler()
        normalized_df[numeric_cols] = scaler.fit_transform(normalized_df[numeric_cols])
        
    else:
        normalized_df = df.copy()
        col_min = normalized_df[numeric_cols].min()
        col_max = normalized_df[numeric_cols].max()
        normalized_df[numeric_cols] = (normalized_df[numeric_cols] - col_min) / (col_max - col_min)
    
    return normalized_df

def get_year_snapshot(year: int) -> pd.DataFrame:
    """
    Get all countries for the specified year. 
    Does not require completeness across all years
    """
    res_data = ld.map_data()
    df_year = res_data[f'res_{year}']
    return df_year

def remove_excessive_features(df: pd.DataFrame) -> pd.DataFrame:
    """
    Remove excessive features, Dimensity Reduction
    Returns DataFrame with 14 features
    """
    columns_to_remove = [
        # Cost of living
        'Cost of Living Plus Rent Index',
        'Groceries Index',
        'Restaurant Price Index',
        'Rent Index',
        
        # Health
        'Health CareExp. Index',
        
        # Pollution
        'Exp Pollution Index',
        
        # Property
        'Gross Rental Yield Outside of Centre',
        'Price To Rent Ratio Outside Of City Centre',
        'Mortgage As A Percentage Of Income',
        
        # Traffic
        'Time Index(in minutes)',
        'Inefficiency Index',
        'Time Exp. Index',
        
        # Crime
        'Safety Index'       
    ]
    
    res_df = df.drop(columns=columns_to_remove)
    return res_df

def main() -> None:
    df = intersection_all()
    print("Original DataFrame:")
    validate_data(df)
    
    df_normalized = normalize_data(df=df, method="standard")
    print("\nNormalized DataFrame:")
    validate_data(df_normalized)
    
    df_2025_all = get_year_snapshot(2025)
    print(f"Countries in 2025: {len(df_2025_all)}")
    df_2025_cut = remove_excessive_features(df_2025_all)
    print(f"Shape: {df_2025_cut.shape}")
    
    df_2026_all = get_year_snapshot(2026)
    print(f"Countries in 2026: {len(df_2026_all)}")
    


if __name__ == '__main__':
    main()