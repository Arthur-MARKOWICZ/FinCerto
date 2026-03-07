"""
Módulo de autenticação JWT para o serviço de Analytics.

Valida o token JWT no cabeçalho Authorization das requisições,
utilizando o mesmo segredo JWT do serviço Java (finanCertoBack).
"""

import os
import hmac
import hashlib
import base64
import json
import logging
from pathlib import Path
from typing import Optional

from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from dotenv import load_dotenv

# Carregar .env da raiz do projeto (sobe até 5 níveis)
_current = Path(__file__).resolve().parent
for _ in range(5):
    _env_candidate = _current / ".env"
    if _env_candidate.exists():
        load_dotenv(dotenv_path=str(_env_candidate))
        break
    _current = _current.parent

logger = logging.getLogger(__name__)

security = HTTPBearer()

JWT_SECRET: str = os.getenv(
    "JWT_SECRET",
    "minha-chave-secreta-super-segura-que-deve-ter-pelo-menos-256-bits",
)


def _base64url_decode(data: str) -> bytes:
    """Decodifica uma string base64url (sem padding)."""
    padding = 4 - len(data) % 4
    if padding != 4:
        data += "=" * padding
    return base64.urlsafe_b64decode(data)


def _verify_hs256(token: str, secret: str) -> dict:
    """
    Verifica e decodifica um JWT assinado com HS256.

    Args:
        token: O token JWT completo (header.payload.signature).
        secret: A chave secreta usada para assinar o token.

    Returns:
        O payload decodificado como dicionário.

    Raises:
        ValueError: Se o token for inválido ou a assinatura não corresponder.
    """
    parts = token.split(".")
    if len(parts) != 3:
        raise ValueError("Token JWT malformado")

    header_b64, payload_b64, signature_b64 = parts

    # Verificar assinatura
    signing_input = f"{header_b64}.{payload_b64}".encode("utf-8")
    secret_bytes = secret.encode("utf-8")
    expected_signature = hmac.new(
        secret_bytes, signing_input, hashlib.sha256
    ).digest()
    actual_signature = _base64url_decode(signature_b64)

    if not hmac.compare_digest(expected_signature, actual_signature):
        raise ValueError("Assinatura JWT inválida")

    # Decodificar payload
    payload_json = _base64url_decode(payload_b64).decode("utf-8")
    return json.loads(payload_json)


async def get_current_user_id(
    credentials: HTTPAuthorizationCredentials = Depends(security),
) -> int:
    """
    Dependency do FastAPI que extrai e valida o token JWT.

    Retorna o user_id (campo 'sub') contido no payload do token.

    Args:
        credentials: Credenciais HTTP Bearer extraídas automaticamente pelo FastAPI.

    Returns:
        O ID do usuário autenticado.

    Raises:
        HTTPException 401: Se o token for inválido, expirado ou ausente.
    """
    token = credentials.credentials

    try:
        payload = _verify_hs256(token, JWT_SECRET)
    except ValueError as e:
        logger.warning(f"Token JWT inválido: {e}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Token inválido: {str(e)}",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Extrair user_id do campo 'sub'
    user_id_str: Optional[str] = payload.get("sub")
    if not user_id_str:
        logger.warning("Token JWT sem campo 'sub'")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token não contém identificador do usuário",
            headers={"WWW-Authenticate": "Bearer"},
        )

    try:
        return int(user_id_str)
    except (ValueError, TypeError):
        logger.warning(f"Campo 'sub' do JWT não é um inteiro válido: {user_id_str}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Identificador de usuário inválido no token",
            headers={"WWW-Authenticate": "Bearer"},
        )
