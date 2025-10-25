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
            buffer, mime, filename = await self.report_service.gerar_relatorio_por_categoria(
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
    # Adicione este método DENTRO da classe ReportController
    async def gerar_relatorio_saldo_mensal(
        self,
        usuario_id: int,
        ano: Optional[int] = None,
        conta_id: Optional[int] = None,
        formato: str = "excel"
    ):
        """
        Gera um relatório de saldo mensal mostrando receitas, despesas e saldo por mês.
        
        Args:
            usuario_id: ID do usuário
            ano: Ano para filtrar (opcional)
            conta_id: ID da conta bancária (opcional)
            formato: Formato do relatório (excel ou pdf)
        """
        try:
            buffer, mime, filename = await self.report_service.gerar_relatorio(
                report_type='saldo_mensal',
                format_type=formato,
                usuario_id=usuario_id,
                ano=ano,
                conta_id=conta_id
            )
            return StreamingResponse(
                buffer,
                media_type=mime,
                headers={"Content-Disposition": f"attachment; filename={filename}"}
            )
        except Exception as e:
            raise HTTPException(status_code=400, detail=str(e))
    async def gerar_relatorio_transacao_detalhado(
        self,
        usuario_id: int,
        conta_id:  Optional[int] = None,
        categoria_id: Optional[int] = None,
        data_inicio:  Optional[int] = None,
        data_fim: Optional[int] = None,
        valor_minimo:  Optional[int] = None,
        valor_maximo:  Optional[int] = None,
        tipo_transacao: Optional[str] = None,
        formato: str = "excel"):

        try:
            buffer, mime, filename = await self.report_service.gerar_relatorio(
                report_type='transacoes_detalhadas',
                format_type=formato,
                usuario_id=usuario_id,
                conta_id=conta_id,
                categoria_id=categoria_id,
                data_inicio=data_inicio,
                data_fim=data_fim,
                valor_minimo=valor_minimo,
                valor_maximo=valor_maximo,
                tipo_transacao=tipo_transacao
            )
            return StreamingResponse(
                buffer,
                media_type=mime,
                headers={"Content-Disposition": f"attachment; filename={filename}"}
            )
        except Exception as e:
            raise HTTPException(status_code=400, detail=str(e))
def get_report_controller(db: Session = Depends(get_db)) -> ReportController:
    return ReportController(db)


@router.get("/relatorioPorCategoria")
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


@router.get("/relatorioSaldoMensal")
async def relatorio_saldo_mensal(
    usuario_id: int,
    ano: Optional[int] = Query(None, description="Ano para filtrar (opcional)"),
    conta_id: Optional[int] = Query(None, description="ID da conta bancária (opcional)"),
    formato: str = Query("excel", description="Formato do relatório (excel ou pdf)"),
    controller: ReportController = Depends(get_report_controller)
):
    """
    Gera um relatório de saldo mensal mostrando receitas, despesas e saldo por mês.
    Filtros opcionais por ano e conta bancária.
    """
    return await controller.gerar_relatorio_saldo_mensal(
        usuario_id=usuario_id,
        ano=ano,
        conta_id=conta_id,
        formato=formato
    )
@router.get("/relatorioTransacaoDetalhado")
async def relatorio_transacao_detalhado(
    usuario_id: int,
    conta_id: Optional[int] = Query(None, description="ID da conta bancária (opcional)"),
    categoria_id: Optional[int] = Query(None, description="ID da categoria (opcional)"),
    data_inicio: Optional[str] = Query(None, description="Data de início (opcional)"),
    data_fim: Optional[str] = Query(None, description="Data de fim (opcional)"),
    valor_minimo: Optional[float] = Query(None, description="Valor mínimo (opcional)"),
    valor_maximo: Optional[float] = Query(None, description="Valor máximo (opcional)"),
    tipo_transacao: Optional[str] = Query(None, description="Tipo de transação (opcional)"),
    formato: str = Query("excel", description="Formato do relatório (excel ou pdf)"),
    controller: ReportController = Depends(get_report_controller)
):
    return await controller.gerar_relatorio_transacao_detalhado(
        usuario_id=usuario_id,
        conta_id=conta_id,
        categoria_id=categoria_id,
        data_inicio=data_inicio,
        data_fim=data_fim,
        valor_minimo=valor_minimo,
        valor_maximo=valor_maximo,
        tipo_transacao=tipo_transacao,
        formato=formato
    )