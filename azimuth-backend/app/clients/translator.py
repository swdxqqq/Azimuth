from typing import List, Optional
from time import sleep
from deep_translator import MyMemoryTranslator


class TranslatorClient:
    RATE_LIMIT = 1.0
    
    def translate(self, text: str, source: str = 'en') -> Optional[str]:
        try:
            t = MyMemoryTranslator(source=f'{source}-{source.upper()}', target='en-GB')
            return t.translate(text)
        except Exception:
            return None
    
    def translate_batch(self, texts: List[str], source: str = 'auto') -> List[Optional[str]]:
        results = []
        for text in texts:
            results.append(self.translate(text, source))
            sleep(self.RATE_LIMIT)
        return results


if __name__ == "__main__":
    import sys
    sys.stdout.reconfigure(encoding='utf-8')
    
    client = TranslatorClient()
    tests = [
        ("Allemagne: croissance economique", "fr"),
        ("Deutschland: Wirtschaft wachst stark", "de"),
        ("Alemania: economia en crecimiento", "es"),
    ]
    
    for text, lang in tests:
        result = client.translate(text, source=lang)
        print(f"  [{lang}] {text[:35]} -> {result}")
