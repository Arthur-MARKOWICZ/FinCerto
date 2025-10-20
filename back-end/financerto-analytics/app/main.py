import sys
from pathlib import Path

sys.path.append(str(Path(__file__).resolve().parent.parent))
from fastapi import FastAPI, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text
from app.database import get_db
import sys


app = FastAPI()

@app.get("/")
def home():
    return {"message": "FinanCerto Analytics conectado!"}

@app.get("/test-db")
def test_db(db: Session = Depends(get_db)):
    result = db.execute(text("SELECT NOW();"))
    data = result.scalar()
    return {"db_time": data}
