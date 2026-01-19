import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from dotenv import load_dotenv

load_dotenv()


def build_database_url() -> str:
    """
    Build the database URL with a safe fallback.
    - Prefer a full DATABASE_URL (as provided in docker-compose).
    - Fallback to DB_* parts if DATABASE_URL is missing.
    """
    env_url = os.getenv("DATABASE_URL")
    if env_url:
        # SQLAlchemy expects "postgresql+psycopg2" instead of "postgres"
        return env_url.replace("postgres://", "postgresql+psycopg2://", 1)

    db_user = os.getenv("DB_USER")
    db_password = os.getenv("DB_PASSWORD")
    db_host = os.getenv("DB_HOST")
    db_port = os.getenv("DB_PORT")
    db_name = os.getenv("DB_NAME")

    # Simple validation to avoid None ending up in the URL
    if not all([db_user, db_password, db_host, db_port, db_name]):
        raise RuntimeError("Database configuration is incomplete. Please set DATABASE_URL or DB_* variables.")

    return f"postgresql+psycopg2://{db_user}:{db_password}@{db_host}:{db_port}/{db_name}"


DATABASE_URL = build_database_url()

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
