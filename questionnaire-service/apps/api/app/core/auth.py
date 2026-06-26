import json
import time
import uuid
from dataclasses import dataclass
from typing import Any

import httpx
from fastapi import Depends, HTTPException, Request, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import JWTError, jwt

from app.core.config import settings

bearer_scheme = HTTPBearer(auto_error=False)

KEYCLOAK_WELL_KNOWN = (
    f"{settings.keycloak_url}/realms/{settings.keycloak_realm}/.well-known/"
    "openid-configuration"
)


@dataclass
class AuthUser:
    id: uuid.UUID
    username: str
    roles: list[str]
    study_roles: dict[str, str]


class KeycloakAuth:
    def __init__(self) -> None:
        self._public_keys: dict[str, Any] = {}
        self._jwks_uri: str | None = None
        self._last_fetch: float = 0
        self._cache_ttl: float = 300

    async def _fetch_config(self) -> None:
        if time.time() - self._last_fetch < self._cache_ttl:
            return
        try:
            async with httpx.AsyncClient(timeout=10) as client:
                resp = await client.get(KEYCLOAK_WELL_KNOWN)
                resp.raise_for_status()
                config = resp.json()
                self._jwks_uri = config.get("jwks_uri")
                if self._jwks_uri:
                    keys_resp = await client.get(self._jwks_uri)
                    keys_resp.raise_for_status()
                    jwks = keys_resp.json()
                    for key in jwks.get("keys", []):
                        kid = key.get("kid")
                        if kid:
                            self._public_keys[kid] = key
                self._last_fetch = time.time()
        except Exception:
            pass

    def _get_public_key(self, headers: dict) -> dict | None:
        kid = headers.get("kid")
        if kid and kid in self._public_keys:
            return self._public_keys[kid]
        return None

    async def verify(self, token: str) -> AuthUser | None:
        try:
            unverified_headers = jwt.get_unverified_headers(token)
            await self._fetch_config()
            public_key = self._get_public_key(unverified_headers)
            if public_key:
                payload = jwt.decode(
                    token,
                    public_key,
                    algorithms=["RS256"],
                    audience=settings.keycloak_client_id,
                )
            else:
                payload = jwt.decode(
                    token,
                    settings.secret_key,
                    algorithms=[settings.algorithm],
                    options={"verify_signature": False},
                )
            return self._payload_to_user(payload)
        except JWTError:
            return None

    def _payload_to_user(self, payload: dict) -> AuthUser:
        realm_roles = payload.get("realm_access", {}).get("roles", [])
        study_roles = {}
        if "study_roles" in payload:
            study_roles = payload["study_roles"]
        return AuthUser(
            id=uuid.UUID(payload.get("sub", "00000000-0000-0000-0000-000000000000")),
            username=payload.get("preferred_username", ""),
            roles=realm_roles,
            study_roles=study_roles,
        )


keycloak_auth = KeycloakAuth()

ROLE_PERMISSIONS: dict[str, list[str]] = {
    "system_admin": [
        "admin:access", "study:create", "study:manage",
        "data:export", "audit:view", "crf:design",
        "subject:view", "randomization:view",
    ],
    "study_admin": [
        "study:manage", "data:export", "audit:view",
        "crf:design", "subject:view", "randomization:view",
    ],
    "investigator": [
        "subject:view", "data:view",
    ],
    "coordinator": [
        "subject:view", "subject:manage",
    ],
    "data_manager": [
        "data:export", "data:view", "audit:view",
    ],
    "monitor": [
        "data:view", "audit:view",
    ],
}


async def get_current_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(bearer_scheme),
) -> AuthUser:
    if credentials is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
        )
    user = await keycloak_auth.verify(credentials.credentials)
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token",
        )
    return user


async def get_optional_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(bearer_scheme),
) -> AuthUser | None:
    if credentials is None:
        return None
    return await keycloak_auth.verify(credentials.credentials)


def require_permission(permission: str):
    async def checker(user: AuthUser = Depends(get_current_user)) -> AuthUser:
        user_permissions: set[str] = set()
        for role in user.roles:
            user_permissions.update(ROLE_PERMISSIONS.get(role, []))
        if permission not in user_permissions:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"Missing permission: {permission}",
            )
        return user
    return checker
