from fastapi import FastAPI, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text

# Usar imports relativos para funcionar bem quando o módulo é carregado como "app.main"
from .database import get_db
from .report_controller import router as relatorio_router


app = FastAPI(title="FinanCerto Reports API")
app.include_router(relatorio_router, prefix="/api", tags=["Relatórios"])


@app.get("/")
def home():
    return {"message": "FinanCerto Analytics conectado!"}


@app.get("/test-db")
def test_db(db: Session = Depends(get_db)):
    result = db.execute(text("SELECT NOW();"))
    data = result.scalar()
    return {"db_time": data}
