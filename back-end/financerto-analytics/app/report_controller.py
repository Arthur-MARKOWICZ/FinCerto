from fastapi import APIRouter, Depends, Query, HTTPException
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session
from typing import Optional
from .database import SessionLocal
from .report_service import ReportService

router = APIRouter()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

class ReportController:
    def __init__(self, db: Session = Depends(get_db)):
        self.db = db
        from .report_repository import ReportRepository
        self.report_service = ReportService(ReportRepository(db))

    async def gerar_relatorio(
        self,
        usuario_id: int,
        categoria_id: Optional[int] = None,
        tipo: Optional[str] = None,
        periodo: Optional[str] = None,
        formato: str = "excel"
    ):
        try:
            buffer, mime, filename = await self.report_service.gerar_relatorio(
                usuario_id=usuario_id,
                categoria_id=categoria_id,
                tipo=tipo,
                periodo=periodo,
                formato=formato
            )
            return StreamingResponse(
                buffer,
                media_type=mime,
                headers={"Content-Disposition": f"attachment; filename={filename}"}
            )
        except Exception as e:
            raise HTTPException(status_code=400, detail=str(e))

# Create controller instance with dependency injection
def get_report_controller(db: Session = Depends(get_db)) -> ReportController:
    return ReportController(db)

# API Routes
@router.get("/relatorio")
async def gerar_relatorio(
    usuario_id: int,
    categoria_id: Optional[int] = Query(None),
    tipo: Optional[str] = Query(None),
    periodo: Optional[str] = Query(None),
    formato: str = Query("excel"),
    controller: ReportController = Depends(get_report_controller)
):
    return await controller.gerar_relatorio(
        usuario_id=usuario_id,
        categoria_id=categoria_id,
        tipo=tipo,
        periodo=periodo,
        formato=formato
    )
