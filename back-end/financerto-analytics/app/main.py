from fastapi import FastAPI, Depends
from sqlalchemy.orm import Session
from app.database import get_db

app = FastAPI()

@app.get("/")
def home():
    return {"message": "FinanCerto Analytics conectado!"}

@app.get("/test-db")
def test_db(db: Session = Depends(get_db)):
    result = db.execute("SELECT NOW();")
    data = result.fetchone()
    return {"db_time": data[0]}
