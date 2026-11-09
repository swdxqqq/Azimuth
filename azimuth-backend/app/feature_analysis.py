import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import data_analysis as da

def analyze_feature_groups(df: pd.DataFrame) -> None:
    """Examine the interrelationships within thematic groups"""
    feature_groups = {
        'cost_of_living': ['Cost of Living Index', 'Rent Index', 
                           'Cost of Living Plus Rent Index', 'Groceries Index',
                           'Restaurant Price Index', 'Local Purchasing Power Index'],
        'crime': ['Crime Index', 'Safety Index'],
        'health': ['Health Care Index', 'Health CareExp. Index'],
        'pollution': ['Pollution Index', 'Exp Pollution Index'],
        'property': ['Price To Income Ratio', 'Gross Rental Yield City Centre',
                     'Gross Rental Yield Outside of Centre', 'Price To Rent Ratio City Centre',
                     'Price To Rent Ratio Outside Of City Centre',
                     'Mortgage As A Percentage Of Income', 'Affordability Index'],
        'traffic': ['Traffic Index', 'Time Index(in minutes)', 
                    'Time Exp. Index', 'Inefficiency Index', 'CO2 Emission Index']
    }
    
    for group_name, cols in feature_groups.items():
        group_df = df[cols]
        corr = group_df.corr() # Compute correlation matrix
        print(f"CORRELATION MATRIX FOR {group_name.upper()}\n")

        high_corr = (corr.stack().reset_index().rename(columns={'level_0': 'Feature 1', 'level_1': 'Feature 2', 0: 'Correlation'})) # Transform to long format for easier filtering
        
        high_corr = high_corr[
            (high_corr['Feature 1'] != high_corr['Feature 2']) &
            (high_corr['Correlation'] > 0.9)
        ].reset_index(drop=True) # Filter for high correlations, excluding self-correlations

        if not high_corr.empty:
            print(f"Highly correlated pairs (>0.9):\n{high_corr}\n")
            
       
def main():
    df_normalized = da.normalize_data("standard")
    df = df_normalized[df_normalized['Year'] == 2025].copy()
    analyze_feature_groups(df)
       
            
if __name__ == "__main__":
    main()