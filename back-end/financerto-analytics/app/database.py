"""
Configuração de conexão com o banco de dados PostgreSQL.

Este módulo centraliza a criação do engine e da sessão do SQLAlchemy,
seguindo o princípio DIP (Dependency Inversion) para facilitar a troca
entre ambientes (localhost, produção).
"""

import os
from pathlib import Path
from typing import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker
from dotenv import load_dotenv


def _find_dotenv() -> Path:
    """
    Encontra o arquivo .env subindo a árvore de diretórios a partir deste arquivo.

    Percorre até 5 níveis acima procurando um .env na raiz do projeto.

    Returns:
        Path para o .env encontrado, ou Path vazio se não existir.
    """
    current = Path(__file__).resolve().parent
    for _ in range(5):
        env_path = current / ".env"
        if env_path.exists():
            return env_path
        current = current.parent
    return Path()


_env_path = _find_dotenv()
if _env_path.exists():
    load_dotenv(dotenv_path=str(_env_path))
else:
    load_dotenv()  # fallback: tenta o diretório atual


def build_database_url() -> str:
    """
    Constrói a URL de conexão com o banco de dados.

    Prioriza a variável DATABASE_URL (docker-compose).
    Caso ausente, monta a URL a partir das variáveis DB_* individuais.

    Returns:
        URL de conexão formatada para SQLAlchemy (postgresql+psycopg2).

    Raises:
        RuntimeError: Se as variáveis de ambiente estiverem incompletas.
    """
    env_url = os.getenv("DATABASE_URL")
    if env_url:
        return env_url.replace("postgres://", "postgresql+psycopg2://", 1)

    db_user = os.getenv("DB_USER") or os.getenv("DB_USERNAME")
    db_password = os.getenv("DB_PASSWORD")
    db_host = os.getenv("DB_HOST")
    db_port = os.getenv("DB_PORT")
    db_name = os.getenv("DB_NAME")

    if not all([db_user, db_password, db_host, db_port, db_name]):
        raise RuntimeError(
            "Configuração de banco de dados incompleta. "
            "Defina DATABASE_URL ou as variáveis DB_USER, DB_PASSWORD, "
            "DB_HOST, DB_PORT e DB_NAME."
        )

    return f"postgresql+psycopg2://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}"


DATABASE_URL: str = build_database_url()

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def get_db() -> Generator[Session, None, None]:
    """
    Dependency do FastAPI que fornece uma sessão de banco de dados.

    Cria uma nova sessão para cada requisição e a fecha automaticamente
    ao final, garantindo que conexões não fiquem abertas.

    Yields:
        Session: Sessão do SQLAlchemy para operações de banco.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
