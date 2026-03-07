import os
from pathlib import Path
from dotenv import load_dotenv

def _find_dotenv() -> Path:
    current = Path(__file__).resolve().parent
    print(f"Starting at: {current}")
    for i in range(5):
        env_path = current / ".env"
        print(f"Checking: {env_path}")
        if env_path.exists():
            print(f"Found .env at: {env_path}")
            return env_path
        current = current.parent
    return Path()

_env_path = _find_dotenv()
if _env_path.exists():
    load_dotenv(dotenv_path=str(_env_path))
    print(f"DB_USER from .env: {os.getenv('DB_USER')}")
else:
    print(".env NOT FOUND")
