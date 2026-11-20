# Azimuth

Backend платформа для анализа и сравнения стран с рекомендательной системой для выбора места проживания.

## Технологии

- **FastAPI** — веб-фреймворк
- **PostgreSQL** — база данных
- **SQLAlchemy** — ORM
- **Pydantic** — валидация данных
- **JWT** — аутентификация
- **pandas, scikit-learn** — анализ данных и ML
- **matplotlib, seaborn** — визуализация

## Установка

```bash
pip install -r requirements.txt
```

## Настройка

Создайте файл `.env`:

```bash
touch .env
```

Заполните переменные окружения:
- `DATABASE_URL` — строка подключения к PostgreSQL
- `JWT_SECRET_KEY` — секретный ключ для JWT токенов
- `CURRENTS_API_KEY` — ключ API для Currents (опционально)

## Запуск

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

API документация: http://localhost:8000/docs

## Бесплатный демо-деплой на Render

> **Ограничения бесплатного тарифа:** Render Free Web Service останавливается после 15 минут без запросов и обычно запускается около минуты при следующем запросе. Бесплатная PostgreSQL-база ограничена 1 ГБ, истекает через 30 дней после создания и после дополнительных 14 дней ожидания удаляется вместе с данными. У бесплатной базы нет резервных копий. Перед истечением срока скачайте нужные данные или согласитесь на их потерю. Этот вариант подходит для демонстрации, не для постоянного хранения пользовательских данных.

### 1. Создайте PostgreSQL

В Render Dashboard выберите **New → PostgreSQL**:

- имя, например `azimuth-db`;
- тариф **Free** и размер 1 ГБ;
- выберите регион, в котором затем создадите API-сервис;
- не включайте автоматическое увеличение диска.

После создания откройте **Connect** и скопируйте **Internal Database URL**. Используйте его только в переменной окружения; не добавляйте строку подключения в Git и не отправляйте её в чат.

### 2. Создайте API-сервис

Выберите **New → Web Service**, подключите GitHub-репозиторий и укажите:

- ветку `feature` (или актуальную ветку для деплоя);
- **Root Directory:** `azimuth-backend`;
- **Language:** Docker;
- **Dockerfile Path:** `Dockerfile`;
- **Instance Type:** Free;
- тот же регион, что у базы.

Добавьте переменные окружения:

- `DATABASE_URL` — Internal Database URL из Render PostgreSQL;
- `JWT_SECRET_KEY` — новый случайный секрет для JWT;
- `CURRENTS_API_KEY` — необязательный ключ Currents для новостей.

Dockerfile применяет миграции Alembic, заполняет пустую базу странами из CSV и запускает Uvicorn на порту Render. Повторный старт не импортирует страны повторно.

### 3. Проверьте API

После успешного деплоя откройте `https://<имя-сервиса>.onrender.com/` и `https://<имя-сервиса>.onrender.com/api/v1/countries?limit=2`. Первый запрос после простоя может ждать запуска сервиса.

## Миграции базы данных

Проект использует Alembic для управления миграциями PostgreSQL.

### Применение миграций

Для применения всех миграций:

```bash
alembic upgrade head
```

Для отката последней миграции:

```bash
alembic downgrade -1
```

Для просмотра статуса миграций:

```bash
alembic current
```

### Заполнение базы данных

Для заполнения базы данных начальными данными (86 стран + метрики):

```bash
python seed_data.py
```

Этот скрипт:
- Создаёт таблицы из SQLAlchemy моделей
- Загружает данные стран из `data/merged_all_features_2025.csv`
- Добавляет метрики для каждой страны (cost_of_living, rent, safety, etc.)

## API Endpoints

### Публичные (без авторизации)
- `GET /api/v1/countries` — список стран
- `GET /api/v1/countries/{id}` — детальная информация о стране
- `POST /api/v1/recommend` — персональные рекомендации

### Аутентификация
- `POST /api/v1/auth/register` — регистрация
- `POST /api/v1/auth/login` — вход

### Защищённые (требуют JWT)
- `GET /api/v1/user/preferences` — получить предпочтения
- `PUT /api/v1/user/preferences` — обновить предпочтения
- `GET /api/v1/user/favorites` — список избранных стран
- `POST /api/v1/user/favorites/{id}` — добавить в избранное
- `DELETE /api/v1/user/favorites/{id}` — удалить из избранного

## Структура проекта

```
azimuth/
├── app/
│   ├── api/                    # API endpoints
│   │   ├── auth.py             # Регистрация, логин
│   │   ├── countries.py        # Список стран, детали
│   │   ├── recommend.py        # Рекомендации
│   │   └── user.py             # Предпочтения, избранное
│   ├── clients/                # Внешние API клиенты
│   │   ├── base.py             # Базовый HTTP клиент
│   │   ├── currents.py         # Новости
│   │   ├── frankfurter.py      # Курсы валют
│   │   ├── worldbank.py        # Экономические данные
│   │   └── translator.py       # Перевод
│   ├── models/                 # SQLAlchemy модели
│   │   ├── country.py          # Country, Metric
│   │   ├── user.py             # User, UserPreference, FavoriteCountry
│   │   └── metrics.py          # EconomicIndicator, NewsArticle
│   ├── schemas/                # Pydantic схемы
│   │   ├── auth.py             # Схемы аутентификации
│   │   ├── country.py          # Схемы стран
│   │   ├── recommend.py        # Схемы рекомендаций
│   │   └── user.py             # Схемы пользователей
│   ├── services/               # Бизнес-логика
│   │   └── recsys_adapter.py   # Адаптер RecSys для БД
│   ├── recsys/                 # Рекомендательная система
│   │   ├── config.py           # Конфиг RecSys
│   │   ├── engine.py           # Движок рекомендаций
│   │   └── tests.py            # Тесты
│   ├── utils/
│   │   └── security.py         # JWT, хэширование паролей
│   ├── clustering.py           # Кластеризация (K-Means)
│   ├── data_analysis.py        # Анализ данных
│   ├── feature_analysis.py     # Анализ признаков
│   ├── load_data.py            # Загрузка CSV
│   ├── config.py               # Настройки приложения
│   ├── database.py             # Подключение к БД
│   ├── dependencies.py         # FastAPI зависимости
│   └── main.py                 # Точка входа
├── alembic/                    # Миграции базы данных
│   ├── versions/               # Файлы миграций
│   ├── env.py                  # Конфигурация Alembic
│   └── script.py.mako          # Шаблон миграций
├── data/                       # CSV данные стран
├── .env                        # Переменные окружения (gitignored)
├── .gitignore
├── alembic.ini                 # Конфигурация Alembic
├── README.md
├── requirements.txt
└── seed_data.py                # Скрипт заполнения БД
```
