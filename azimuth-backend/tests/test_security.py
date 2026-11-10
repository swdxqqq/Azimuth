import pytest
from datetime import timedelta
from app.utils.security import (
    verify_password,
    get_password_hash,
    create_access_token,
    decode_access_token,
)


class TestPasswordHashing:
    def test_hash_and_verify_correct_password(self):
        password = "SecurePass123!"
        hashed = get_password_hash(password)
        assert hashed != password
        assert verify_password(password, hashed)

    def test_verify_wrong_password(self):
        password = "SecurePass123!"
        hashed = get_password_hash(password)
        assert not verify_password("WrongPassword!", hashed)

    def test_different_hashes_for_same_password(self):
        password = "SecurePass123!"
        hash1 = get_password_hash(password)
        hash2 = get_password_hash(password)
        assert hash1 != hash2

    def test_hash_is_not_plaintext(self):
        password = "mypassword"
        hashed = get_password_hash(password)
        assert "mypassword" not in hashed


class TestJWTToken:
    def test_create_and_decode_token(self, mock_settings):
        data = {"sub": "42"}
        token = create_access_token(data)
        payload = decode_access_token(token)
        assert payload is not None
        assert payload["sub"] == "42"

    def test_token_has_expiration(self, mock_settings):
        data = {"sub": "1"}
        token = create_access_token(data)
        payload = decode_access_token(token)
        assert "exp" in payload

    def test_custom_expiration_delta(self, mock_settings):
        data = {"sub": "1"}
        delta = timedelta(hours=2)
        token = create_access_token(data, expires_delta=delta)
        payload = decode_access_token(token)
        assert payload is not None

    def test_decode_invalid_token(self, mock_settings):
        result = decode_access_token("invalid.token.string")
        assert result is None

    def test_decode_expired_token(self, mock_settings):
        data = {"sub": "1"}
        delta = timedelta(seconds=-1)
        token = create_access_token(data, expires_delta=delta)
        result = decode_access_token(token)
        assert result is None

    def test_token_payload_preserves_data(self, mock_settings):
        data = {"sub": "99", "role": "admin"}
        token = create_access_token(data)
        payload = decode_access_token(token)
        assert payload["sub"] == "99"
        assert payload["role"] == "admin"
