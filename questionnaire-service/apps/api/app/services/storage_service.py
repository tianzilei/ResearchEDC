import io
from datetime import timedelta

from app.core.config import settings


class StorageService:
    def __init__(self) -> None:
        self._client = None

    async def _get_client(self):
        if self._client is None:
            try:
                from minio import Minio
                self._client = Minio(
                    settings.minio_endpoint,
                    access_key=settings.minio_access_key,
                    secret_key=settings.minio_secret_key,
                    secure=settings.minio_secure,
                )
                bucket = settings.minio_bucket
                if not self._client.bucket_exists(bucket):
                    self._client.make_bucket(bucket)
            except Exception:
                self._client = None
        return self._client

    async def upload(self, key: str, data: io.BytesIO, content_type: str = "application/octet-stream") -> str | None:
        client = await self._get_client()
        if client is None:
            import os
            local_path = f"/tmp/exports/{key}"
            os.makedirs(os.path.dirname(local_path), exist_ok=True)
            with open(local_path, "wb") as f:
                f.write(data.getvalue())
            return local_path
        data.seek(0)
        client.put_object(
            settings.minio_bucket,
            key,
            data,
            length=data.getbuffer().nbytes,
            content_type=content_type,
        )
        url = client.presigned_get_object(
            settings.minio_bucket,
            key,
            expires=timedelta(hours=24),
        )
        return url

    async def get_download_url(self, key: str) -> str | None:
        client = await self._get_client()
        if client is None:
            return None
        try:
            url = client.presigned_get_object(
                settings.minio_bucket,
                key,
                expires=timedelta(hours=24),
            )
            return url
        except Exception:
            return None


storage_service = StorageService()
