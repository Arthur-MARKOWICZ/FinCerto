"""
Ponto de entrada principal da aplicação FinanCerto Analytics.

Configura o FastAPI com CORS, inclui os routers organizados por domínio,
e define endpoints de health-check.
"""

from fastapi import FastAPI, Depends
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from sqlalchemy import text

from .database import get_db
from .report_controller import router as relatorio_router
from .schemas import MessageResponse, DBTimeResponse

app = FastAPI(
    title="FinanCerto Analytics API",
    description="Serviço de Analytics e Relatórios do FinanCerto",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(relatorio_router, prefix="/api", tags=["Relatórios"])


@app.get("/", response_model=MessageResponse)
async def home() -> MessageResponse:
    """Endpoint de health-check da API."""
    return MessageResponse(message="FinanCerto Analytics conectado!")


@app.get("/test-db", response_model=DBTimeResponse)
async def test_db(db: Session = Depends(get_db)) -> DBTimeResponse:
    """
    Testa a conexão com o banco de dados.

    Executa um SELECT NOW() para validar que o banco está acessível.

    Args:
        db: Sessão do banco de dados injetada via Depends.

    Returns:
        DBTimeResponse com o horário atual do banco.
    """
    result = db.execute(text("SELECT NOW();"))
    data = result.scalar()
    return DBTimeResponse(db_time=str(data))
