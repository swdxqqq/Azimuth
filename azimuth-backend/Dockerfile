FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt \
    && useradd --create-home --uid 10001 azimuth

COPY --chown=azimuth:azimuth . .

USER azimuth

EXPOSE 10000

CMD ["sh", "-c", "alembic upgrade head && python seed_data.py && exec uvicorn app.main:app --host 0.0.0.0 --port ${PORT:-10000}"]
