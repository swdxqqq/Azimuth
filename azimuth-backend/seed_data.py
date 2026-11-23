from pathlib import Path

import pandas as pd
from sqlalchemy.orm import Session
from app.database import engine, SessionLocal, Base
from app.models.country import Country, Metric
from app.models.user import User, UserPreference, FavoriteCountry
from app.models.metrics import EconomicIndicator, NewsArticle
from app.services.country_reference import get_country_reference, load_reference_data

FLAG_ASSETS_DIR = Path(__file__).resolve().parent / "assets" / "flags"
FLAG_ASSET_FILENAMES = {
    asset.name
    for asset in FLAG_ASSETS_DIR.iterdir()
    if asset.is_file() and asset.suffix.lower() == ".svg"
}
FLAG_ASSET_OVERRIDES = {
    "United Kingdom": "GB-UKM - United Kingdom.svg",
}
CLIMATE_SCORE_SOURCE = "Open-Meteo Archive 2020-2024 temperature comfort"


def get_flag_url(code: str, country_name: str) -> str:
    filename = FLAG_ASSET_OVERRIDES.get(
        country_name,
        f"{code} - {country_name}.svg",
    )
    if filename not in FLAG_ASSET_FILENAMES:
        matches = sorted(
            asset_name
            for asset_name in FLAG_ASSET_FILENAMES
            if asset_name.startswith(f"{code} - ")
            or asset_name.startswith(f"{code}-")
        )
        if len(matches) == 1:
            filename = matches[0]
        else:
            print(f"Flag asset not found for {country_name} ({code})")
            return ""

    return f"/assets/flags/{filename}"


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
    populations, languages = load_reference_data(Path(__file__).resolve().parent / "data")
    climate_scores = {
        country_name: float(score)
        for country_name, score in zip(df["Country"], df["climate_comfort_score"])
        if pd.notna(score)
    }
    
    db = SessionLocal()
    
    try:
        existing_country_count = db.query(Country).count()
        if existing_country_count:
            updated_flags = 0
            updated_reference = 0
            updated_climate = 0
            for country in db.query(Country).all():
                flag_url = get_flag_url(country.code, country.name)
                if country.flag_url != flag_url:
                    country.flag_url = flag_url
                    updated_flags += 1

                reference = get_country_reference(
                    country.name,
                    country.code,
                    populations,
                    languages,
                )
                for field, value in reference.items():
                    if value is not None and getattr(country, field) != value:
                        setattr(country, field, value)
                        updated_reference += 1

                climate_score = climate_scores.get(country.name)
                if climate_score is not None:
                    climate_metric = db.query(Metric).filter(
                        Metric.country_id == country.id,
                        Metric.metric_type == "climate",
                    ).order_by(Metric.year.desc()).first()
                    if climate_metric is None:
                        db.add(Metric(
                            country_id=country.id,
                            year=2025,
                            metric_type="climate",
                            value=climate_score,
                            source=CLIMATE_SCORE_SOURCE,
                        ))
                        updated_climate += 1
                    elif (
                        climate_metric.value != climate_score
                        or climate_metric.source != CLIMATE_SCORE_SOURCE
                    ):
                        climate_metric.value = climate_score
                        climate_metric.source = CLIMATE_SCORE_SOURCE
                        updated_climate += 1

            if updated_flags or updated_reference or updated_climate:
                db.commit()
                print(
                    f"Updated {updated_flags} flag URLs, "
                    f"{updated_reference} reference fields, and "
                    f"{updated_climate} climate scores."
                )

            print(
                f"Database already contains {existing_country_count} countries; "
                "skipping initial data import."
            )
            return

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
            reference = get_country_reference(
                country_name,
                code,
                populations,
                languages,
            )
            
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
                flag_url=get_flag_url(code, country_name),
                latitude=latitude,
                longitude=longitude,
                population=reference["population"],
                languages=reference["languages"],
                timezone=reference["timezone"],
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
                    if metric_type == "climate":
                        value = climate_scores.get(country_name, value)
                    if pd.notna(value):
                        metric = Metric(
                            country_id=country.id,
                            year=2025,
                            metric_type=metric_type,
                            value=float(value),
                            source=(
                                CLIMATE_SCORE_SOURCE
                                if metric_type == "climate"
                                else "numbeo"
                                if metric_type not in ("internet_speed", "visa_score")
                                else "other"
                            )
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
