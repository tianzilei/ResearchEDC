import uuid

from app.core.auth import KeycloakAuth


def test_payload_to_user_ignores_email_claim_and_keeps_active_contract():
    user_id = uuid.uuid4()
    payload = {
        "sub": str(user_id),
        "preferred_username": "alice",
        "email": "alice@example.com",
        "realm_access": {"roles": ["study_admin"]},
        "study_roles": {"42": "coordinator"},
    }

    user = KeycloakAuth()._payload_to_user(payload)

    assert user.id == user_id
    assert user.username == "alice"
    assert user.roles == ["study_admin"]
    assert user.study_roles == {"42": "coordinator"}
    assert not hasattr(user, "email")
