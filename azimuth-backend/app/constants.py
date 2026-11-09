from typing import Dict, Set

# ISO 3166-1 alpha-2 country codes
COUNTRY_CODES: Dict[str, str] = {
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
    'UAE': 'AE', 'Saudi Arabia': 'SA', 'Qatar': 'QA',
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

# ISO 3166-1 alpha-3 country codes (для World Bank API)
COUNTRY_ISO3: Dict[str, str] = {
    "Switzerland": "CHE", "Iceland": "ISL", "Singapore": "SGP",
    "Hong Kong (China)": "HKG", "Norway": "NOR", "Denmark": "DNK",
    "Israel": "ISR", "United States": "USA", "Luxembourg": "LUX",
    "Australia": "AUS", "Netherlands": "NLD", "Canada": "CAN",
    "Austria": "AUT", "Belgium": "BEL", "Germany": "DEU",
    "New Zealand": "NZL", "Finland": "FIN", "Sweden": "SWE",
    "Ireland": "IRL", "France": "FRA", "Italy": "ITA",
    "Spain": "ESP", "Japan": "JPN", "South Korea": "KOR",
    "United Kingdom": "GBR", "Portugal": "PRT", "Taiwan": "TWN",
    "Greece": "GRC", "Slovenia": "SVN", "Czech Republic": "CZE",
    "Poland": "POL", "Hungary": "HUN", "Croatia": "HRV",
    "Slovakia": "SVK", "Lithuania": "LTU", "Latvia": "LVA",
    "Estonia": "EST", "Malta": "MLT", "Cyprus": "CYP",
    "Chile": "CHL", "Argentina": "ARG", "Brazil": "BRA",
    "Mexico": "MEX", "Colombia": "COL", "Peru": "PER",
    "Venezuela": "VEN", "Uruguay": "URY", "Costa Rica": "CRI",
    "Panama": "PAN", "India": "IND", "Pakistan": "PAK",
    "Bangladesh": "BGD", "Sri Lanka": "LKA", "Nepal": "NPL",
    "Malaysia": "MYS", "Indonesia": "IDN", "Philippines": "PHL",
    "Thailand": "THA", "Vietnam": "VNM", "China": "CHN",
    "Russia": "RUS", "Ukraine": "UKR", "Turkey": "TUR",
    "Egypt": "EGY", "South Africa": "ZAF", "Nigeria": "NGA",
    "Kenya": "KEN", "Morocco": "MAR", "Tunisia": "TUN",
    "Algeria": "DZA", "Saudi Arabia": "SAU", "United Arab Emirates": "ARE",
    "Qatar": "QAT", "Kuwait": "KWT", "Bahrain": "BHR",
    "Oman": "OMN", "Jordan": "JOR", "Lebanon": "LBN",
    "Iran": "IRN", "Iraq": "IRQ", "Yemen": "YEM",
    "Syria": "SYR", "Bolivia": "BOL", "Ecuador": "ECU",
    "Paraguay": "PRY", "Trinidad and Tobago": "TTO", "Jamaica": "JAM",
    "Dominican Republic": "DOM", "Guatemala": "GTM", "Honduras": "HND",
    "Nicaragua": "NIC", "El Salvador": "SLV", "Albania": "ALB",
    "Armenia": "ARM", "Serbia": "SRB", "Bulgaria": "BGR",
    "Romania": "ROU", "Bosnia And Herzegovina": "BIH", "Georgia": "GEO",
    "North Macedonia": "MKD", "Azerbaijan": "AZE", "Kazakhstan": "KAZ",
    "Belarus": "BLR"
}

# Валюты стран (ISO 4217)
COUNTRY_CURRENCY: Dict[str, str] = {
    "Switzerland": "CHF", "Iceland": "ISK", "Singapore": "SGD",
    "Hong Kong (China)": "HKD", "Norway": "NOK", "Denmark": "DKK",
    "Israel": "ILS", "United States": "USD", "Luxembourg": "EUR",
    "Australia": "AUD", "Netherlands": "EUR", "Canada": "CAD",
    "Austria": "EUR", "Belgium": "EUR", "Germany": "EUR",
    "New Zealand": "NZD", "Finland": "EUR", "Sweden": "SEK",
    "Ireland": "EUR", "France": "EUR", "Italy": "EUR",
    "Spain": "EUR", "Japan": "JPY", "South Korea": "KRW",
    "United Kingdom": "GBP", "Portugal": "EUR", "Taiwan": "TWD",
    "Greece": "EUR", "Slovenia": "EUR", "Czech Republic": "CZK",
    "Poland": "PLN", "Hungary": "HUF", "Croatia": "EUR",
    "Slovakia": "EUR", "Lithuania": "EUR", "Latvia": "EUR",
    "Estonia": "EUR", "Malta": "EUR", "Cyprus": "EUR",
    "Chile": "CLP", "Argentina": "ARS", "Brazil": "BRL",
    "Mexico": "MXN", "Colombia": "COP", "Peru": "PEN",
    "Venezuela": "VES", "Uruguay": "UYU", "Costa Rica": "CRC",
    "Panama": "PAB", "India": "INR", "Pakistan": "PKR",
    "Bangladesh": "BDT", "Sri Lanka": "LKR", "Nepal": "NPR",
    "Malaysia": "MYR", "Indonesia": "IDR", "Philippines": "PHP",
    "Thailand": "THB", "Vietnam": "VND", "China": "CNY",
    "Russia": "RUB", "Ukraine": "UAH", "Turkey": "TRY",
    "Egypt": "EGP", "South Africa": "ZAR", "Nigeria": "NGN",
    "Kenya": "KES", "Morocco": "MAD", "Tunisia": "TND",
    "Algeria": "DZD", "Saudi Arabia": "SAR", "United Arab Emirates": "AED",
    "Qatar": "QAR", "Kuwait": "KWD", "Bahrain": "BHD",
    "Oman": "OMR", "Jordan": "JOD", "Lebanon": "LBP",
    "Iran": "IRR", "Iraq": "IQD", "Yemen": "YER",
    "Syria": "SYP", "Bolivia": "BOB", "Ecuador": "USD",
    "Paraguay": "PYG", "Trinidad and Tobago": "TTD", "Jamaica": "JMD",
    "Dominican Republic": "DOP", "Guatemala": "GTQ", "Honduras": "HNL",
    "Nicaragua": "NIO", "El Salvador": "USD"
}

# Countries by region
REGIONS: Dict[str, Set[str]] = {
    'Europe': {
        'Switzerland', 'Iceland', 'Luxembourg', 'Finland', 'Netherlands',
        'Sweden', 'Germany', 'Austria', 'Belgium', 'Ireland', 'United Kingdom',
        'France', 'Italy', 'Spain', 'Portugal', 'Greece', 'Czech Republic',
        'Slovenia', 'Estonia', 'Poland', 'Croatia', 'Hungary', 'Latvia',
        'Lithuania', 'Malta', 'Romania', 'Slovakia', 'Bulgaria', 'Serbia',
        'Bosnia And Herzegovina', 'Russia', 'Turkey', 'Belarus', 'Ukraine',
        'Moldova', 'North Macedonia', 'Albania', 'Montenegro', 'Cyprus',
        'Georgia', 'Armenia', 'Azerbaijan', 'Kazakhstan'
    },
    'Asia': {
        'Singapore', 'Japan', 'South Korea', 'Taiwan', 'China', 'Thailand',
        'Malaysia', 'Vietnam', 'India', 'Sri Lanka', 'Pakistan', 'Bangladesh',
        'Nepal', 'Indonesia', 'Philippines', 'UAE', 'Saudi Arabia', 'Qatar',
        'Kuwait', 'Bahrain', 'Oman', 'Jordan', 'Lebanon', 'Iran', 'Iraq',
        'Uzbekistan', 'Hong Kong (China)'
    },
    'North America': {
        'United States', 'Canada', 'Mexico', 'Costa Rica', 'Panama',
        'Guatemala', 'Honduras', 'Dominican Republic', 'Jamaica',
        'Trinidad And Tobago'
    },
    'South America': {
        'Argentina', 'Chile', 'Brazil', 'Colombia', 'Peru', 'Ecuador',
        'Uruguay', 'Paraguay', 'Bolivia', 'Venezuela'
    },
    'Africa': {
        'South Africa', 'Morocco', 'Tunisia', 'Algeria', 'Nigeria', 'Kenya',
        'Ghana', 'Senegal', 'Egypt'
    },
    'Oceania': {'Australia', 'New Zealand'}
}

# CSV column to metric type mapping
METRICS_MAPPING: Dict[str, str] = {
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
    'climate': 'climate_comfort_score',
    'internet_speed': 'internet_speed_mbps',
    'visa_score': 'visa_score'
}
