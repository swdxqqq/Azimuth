from functools import reduce
from typing import Dict, Tuple

import pandas as pd

pd.set_option('display.max_columns', None)

# Constants
YEARS = list(range(2014, 2027))
CSV_PATHS = {
    'cost': 'data/cost-of-living_country.csv',
    'crime': 'data/crime_country.csv',
    'health': 'data/health-care_country.csv',
    'pollution': 'data/pollution_country.csv',
    'property': 'data/property-investment_country.csv',
    'traffic': 'data/traffic_country.csv'
}


def load_data() -> Dict[str, pd.DataFrame]:
    """Load all CSV files into DataFrames"""
    return {name: pd.read_csv(path) for name, path in CSV_PATHS.items()}


def map_data() -> Dict[str, pd.DataFrame]:
    """Merge all datasets by year using inner join on Country and Year"""
    datasets = load_data()
    
    # Drop Rank column from all datasets
    for df in datasets.values():
        df.drop('Rank', axis=1, inplace=True)
    
    # Group data by years
    data_by_years = {
        f"{name}_{year}": df[df['Year'] == year]
        for year in YEARS
        for name, df in datasets.items()
    }
    
    # Merge datasets for each year
    res_data = {}
    for year in YEARS:
        dfs_to_merge = [data_by_years[f'{name}_{year}'] for name in CSV_PATHS.keys()]
        merged_df = reduce(
            lambda left, right: pd.merge(left, right, on=['Country', 'Year'], how='inner'),
            dfs_to_merge
        )
        res_data[f'res_{year}'] = merged_df
    
    return res_data


def print_results_by_year() -> None:
    """Print the number of rows for each year"""
    res_data = map_data()
    for year in YEARS:
        df_year = res_data[f'res_{year}']
        print(f"Year {year}: {len(df_year)} rows")


def results_by_year() -> Tuple[pd.DataFrame, ...]:
    """Get all yearly DataFrames as a tuple"""
    res_data = map_data()
    return tuple(res_data[f'res_{year}'] for year in YEARS)

