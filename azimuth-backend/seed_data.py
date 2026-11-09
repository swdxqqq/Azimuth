import pandas as pd
from sqlalchemy.orm import Session
from app.database import engine, SessionLocal, Base
from app.models.country import Country, Metric
from app.models.user import User, UserPreference, FavoriteCountry
from app.models.metrics import EconomicIndicator, NewsArticle


def create_tables():
    print("Creating database tables...")
    Base.metadata.create_all(bind=engine)
    print("Tables created successfully")


def load_countries_data():
    print("\nLoading countries data...")
    
    # Read main data file
    df = pd.read_csv('data/merged_all_features_2025.csv')
    
    # Load capitals for additional info
    capitals_df = pd.read_csv('data/capitals.csv')
    capitals_dict = dict(zip(capitals_df['Country'], capitals_df['Capital']))
    
    # Load internet speed
    internet_df = pd.read_csv('data/internet_speed.csv')
    internet_dict = dict(zip(internet_df['Country'], internet_df['Value']))
    
    # Load visa score
    visa_df = pd.read_csv('data/visa_score.csv')
    visa_dict = dict(zip(visa_df['Country'], visa_df['Value']))
    
    db = SessionLocal()
    
    try:
        for _, row in df.iterrows():
            country_name = row['Country']
            capital = capitals_dict.get(country_name, 'Unknown')
            
            # Extract coordinates from climate data or use defaults
            # For now, using 0,0 - can be updated later
            latitude = 0.0
            longitude = 0.0
            
            # Country codes mapping (ISO 3166-1 alpha-2)
            country_codes = {
                'Switzerland': 'CH', 'Iceland': 'IS', 'Singapore': 'SG', 
                'Hong Kong (China)': 'HK', 'Denmark': 'DK', 'Norway': 'NO',
                'Luxembourg': 'LU', 'Finland': 'FI', 'Netherlands': 'NL',
                'Sweden': 'SE', 'Germany': 'DE', 'Canada': 'CA',
                'Australia': 'AU', 'Austria': 'AT', 'Belgium': 'BE',
                'New Zealand': 'NZ', 'United States': 'US', 'Ireland': 'IE',
                'United Kingdom': 'GB', 'Japan': 'JP', 'France': 'FR',
                'Italy': 'IT', 'Spain': 'ES', 'South Korea': 'KR',
                'Portugal': 'PT', 'Greece': 'GR', 'Czech Republic': 'CZ',
                'Slovenia': 'SI', 'Estonia': 'EE', 'Israel': 'IL',
                'Taiwan': 'TW', 'Poland': 'PL', 'Croatia': 'HR',
                'Hungary': 'HU', 'Chile': 'CL', 'Latvia': 'LV',
                'Lithuania': 'LT', 'Malta': 'MT', 'Romania': 'RO',
                'Slovakia': 'SK', 'Bulgaria': 'BG', 'Serbia': 'RS',
                'Bosnia And Herzegovina': 'BA', 'Uruguay': 'UY',
                'Argentina': 'AR', 'Panama': 'PA', 'Costa Rica': 'CR',
                'Turkey': 'TR', 'Russia': 'RU', 'China': 'CN',
                'Thailand': 'TH', 'Malaysia': 'MY', 'Mexico': 'MX',
                'Brazil': 'BR', 'Colombia': 'CO', 'Peru': 'PE',
                'Ecuador': 'EC', 'Philippines': 'PH', 'Indonesia': 'ID',
                'Vietnam': 'VN', 'India': 'IN', 'Sri Lanka': 'LK',
                'Pakistan': 'PK', 'Bangladesh': 'BD', 'Nepal': 'NP',
                'Egypt': 'EG', 'South Africa': 'ZA', 'Morocco': 'MA',
                'Tunisia': 'TN', 'Algeria': 'DZ', 'Nigeria': 'NG',
                'Kenya': 'KE', 'Ghana': 'GH', 'Senegal': 'SN',
                'UAE': 'AE', 'United Arab Emirates': 'AE', 'Saudi Arabia': 'SA', 'Qatar': 'QA',
                'Kuwait': 'KW', 'Bahrain': 'BH', 'Oman': 'OM',
                'Jordan': 'JO', 'Lebanon': 'LB', 'Iran': 'IR',
                'Iraq': 'IQ', 'Kazakhstan': 'KZ', 'Uzbekistan': 'UZ',
                'Georgia': 'GE', 'Armenia': 'AM', 'Azerbaijan': 'AZ',
                'Belarus': 'BY', 'Ukraine': 'UA', 'Moldova': 'MD',
                'North Macedonia': 'MK', 'Albania': 'AL', 'Montenegro': 'ME',
                'Cyprus': 'CY', 'Paraguay': 'PY', 'Bolivia': 'BO',
                'Venezuela': 'VE', 'Guatemala': 'GT', 'Honduras': 'HN',
                'Dominican Republic': 'DO', 'Jamaica': 'JM', 'Trinidad And Tobago': 'TT'
            }
            
            code = country_codes.get(country_name, 'XX')
            
            # Region mapping
            regions = {
                'Europe': ['Switzerland', 'Iceland', 'Luxembourg', 'Finland', 'Netherlands', 
                          'Sweden', 'Germany', 'Austria', 'Belgium', 'Ireland', 'United Kingdom',
                          'France', 'Italy', 'Spain', 'Portugal', 'Greece', 'Czech Republic',
                          'Slovenia', 'Estonia', 'Poland', 'Croatia', 'Hungary', 'Latvia',
                          'Lithuania', 'Malta', 'Romania', 'Slovakia', 'Bulgaria', 'Serbia',
                          'Bosnia And Herzegovina', 'Russia', 'Turkey', 'Belarus', 'Ukraine',
                          'Moldova', 'North Macedonia', 'Albania', 'Montenegro', 'Cyprus',
                          'Georgia', 'Armenia', 'Azerbaijan', 'Kazakhstan'],
                'Asia': ['Singapore', 'Japan', 'South Korea', 'Taiwan', 'China', 'Thailand',
                        'Malaysia', 'Vietnam', 'India', 'Sri Lanka', 'Pakistan', 'Bangladesh',
                        'Nepal', 'Indonesia', 'Philippines', 'UAE', 'Saudi Arabia', 'Qatar',
                        'Kuwait', 'Bahrain', 'Oman', 'Jordan', 'Lebanon', 'Iran', 'Iraq',
                        'Uzbekistan', 'Hong Kong (China)'],
                'North America': ['United States', 'Canada', 'Mexico', 'Costa Rica', 'Panama',
                                 'Guatemala', 'Honduras', 'Dominican Republic', 'Jamaica',
                                 'Trinidad And Tobago'],
                'South America': ['Argentina', 'Chile', 'Brazil', 'Colombia', 'Peru', 'Ecuador',
                                 'Uruguay', 'Paraguay', 'Bolivia', 'Venezuela'],
                'Africa': ['South Africa', 'Morocco', 'Tunisia', 'Algeria', 'Nigeria', 'Kenya',
                          'Ghana', 'Senegal', 'Egypt'],
                'Oceania': ['Australia', 'New Zealand']
            }
            
            region = 'Unknown'
            for reg, countries in regions.items():
                if country_name in countries:
                    region = reg
                    break
            
            # Create country
            country = Country(
                name=country_name,
                code=code,
                capital=capital,
                region=region,
                flag_url=f"/assets/flags/{code} - {country_name}.svg",
                latitude=latitude,
                longitude=longitude
            )
            db.add(country)
            db.flush()
            
            # Add metrics
            metrics_mapping = {
                'cost_of_living': 'Cost of Living Index',
                'rent': 'Rent Index',
                'groceries': 'Groceries Index',
                'restaurant': 'Restaurant Price Index',
                'purchasing_power': 'Local Purchasing Power Index',
                'crime': 'Crime Index',
                'safety': 'Safety Index',
                'healthcare': 'Health Care Index',
                'pollution': 'Pollution Index',
                'property_price_to_income': 'Price To Income Ratio',
                'traffic': 'Traffic Index',
                'quality_of_life': None,  # Will be calculated
                'climate': 'climate_comfort_score',
                'internet_speed': 'internet_speed_mbps',
                'visa_score': 'visa_score'
            }
            
            for metric_type, csv_column in metrics_mapping.items():
                if csv_column and csv_column in row:
                    value = row[csv_column]
                    if pd.notna(value):
                        metric = Metric(
                            country_id=country.id,
                            year=2025,
                            metric_type=metric_type,
                            value=float(value),
                            source='numbeo' if metric_type != 'climate' and metric_type != 'internet_speed' and metric_type != 'visa_score' else 'other'
                        )
                        db.add(metric)
            
            print(f"Added {country_name} ({code})")
        
        db.commit()
        print(f"\nSuccessfully loaded {len(df)} countries")
        
    except Exception as e:
        db.rollback()
        print(f"Error loading data: {e}")
        raise
    finally:
        db.close()


def main():
    """Main function to populate database."""
    print("=" * 50)
    print("Azimuth Database Population Script")
    print("=" * 50)
    
    # Create tables
    create_tables()
    
    # Load countries data
    load_countries_data()
    
    print("\n" + "=" * 50)
    print("Database population completed!")
    print("=" * 50)


if __name__ == "__main__":
    main()
