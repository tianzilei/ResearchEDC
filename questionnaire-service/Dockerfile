# ResearchEDC Questionnaire Service Dockerfile
# Python FastAPI + uv — deployment image
# Build: docker build -t researchedc-questionnaire -f questionnaire-service/Dockerfile questionnaire-service/apps/api

FROM ghcr.io/astral-sh/uv:python3.12-bookworm-slim AS builder

WORKDIR /app
ENV UV_COMPILE_BYTECODE=1

# Install dependencies (layer cache)
COPY pyproject.toml ./
RUN uv pip install --system -r pyproject.toml

FROM python:3.12-slim-bookworm

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /usr/local/lib/python3.12/site-packages /usr/local/lib/python3.12/site-packages
COPY --from=builder /usr/local/bin /usr/local/bin

# Application code
COPY alembic.ini .
COPY alembic/ alembic/
COPY app/ app/

EXPOSE 8000

HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
    CMD curl -f http://localhost:8000/health || exit 1

CMD ["python", "-m", "uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
