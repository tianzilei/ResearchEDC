from typing import Any
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )

    # Database
    database_url: str = (
        "postgresql+psycopg://researchedf:researchedf@localhost:5432/researchedf_questionnaire"
    )
    database_pool_size: int = 10
    database_max_overflow: int = 20

    # Redis
    redis_url: str = "redis://localhost:6379/0"

    # Security
    secret_key: str = "change-this-to-a-random-secret-key-in-production"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 480

    # Public token
    public_token_expire_hours: int = 72
    public_token_bytes: int = 32

    # Server
    host: str = "0.0.0.0"
    port: int = 8000
    log_level: str = "info"

    # Keycloak
    keycloak_url: str = "http://localhost:8080/auth"
    keycloak_realm: str = "researchedf"
    keycloak_client_id: str = "researchedf-frontend"

    # CORS
    cors_origins: list[str] = ["http://localhost:3000", "http://localhost:5173"]

    # Celery
    celery_broker_url: str = "redis://localhost:6379/0"
    celery_result_backend: str = "redis://localhost:6379/0"

    # MinIO
    minio_endpoint: str = "localhost:9000"
    minio_access_key: str = "minio"
    minio_secret_key: str = "minio-password"
    minio_bucket: str = "questionnaire-exports"
    minio_secure: bool = False


settings = Settings()
