import pandas as pd
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).parent.parent))
import load_data as ld

DATA_DIR = Path(__file__).parent.parent.parent / "data"

def merge_year(year):
    # Load fresh Numbeo data
    res_data = ld.map_data()
    numbeo_df = res_data[f'res_{year}'].copy()
    
    print(f"\n{year} - Original Numbeo: {len(numbeo_df)} countries, {len(numbeo_df.columns)} features")
    
    # Remove Puerto Rico (not in internet data)
    numbeo_df = numbeo_df[numbeo_df['Country'] != 'Puerto Rico'].copy()
    print(f"After removing Puerto Rico: {len(numbeo_df)} countries")
    
    # Load new features
    climate_df = pd.read_csv(DATA_DIR / "climate_features.csv")
    internet_df = pd.read_csv(DATA_DIR / "internet_speed.csv").rename(columns={'Value': 'internet_speed_mbps'})
    visa_df = pd.read_csv(DATA_DIR / "visa_score.csv").rename(columns={'Value': 'visa_score'})
    
    # Merge all
    numbeo_df = numbeo_df.merge(climate_df, on='Country', how='left')
    numbeo_df = numbeo_df.merge(internet_df, on='Country', how='left')
    numbeo_df = numbeo_df.merge(visa_df, on='Country', how='left')
    
    print(f"After merge: {len(numbeo_df)} countries, {len(numbeo_df.columns)} features")
    
    # Check missing values
    print("\nMissing values in new features:")
    for col in ['climate_comfort_score', 'internet_speed_mbps', 'visa_score']:
        missing = numbeo_df[col].isnull().sum()
        if missing > 0:
            missing_countries = numbeo_df[numbeo_df[col].isnull()]['Country'].tolist()
            print(f"  {col}: {missing} missing -> {missing_countries}")
        else:
            print(f"  {col}: 0 missing (OK)")
    
    # Save
    output_file = DATA_DIR / f"merged_all_features_{year}.csv"
    numbeo_df.to_csv(output_file, index=False)
    print(f"\nSaved to {output_file}")
    
    return numbeo_df

if __name__ == "__main__":
    merge_year(2025)
    merge_year(2026)
