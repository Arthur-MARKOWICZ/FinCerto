"""
Controller de relatórios — organiza as rotas da API de geração de relatórios.

Utiliza APIRouter para agrupar endpoints por domínio (/reports).
Todas as rotas são protegidas por JWT e utilizam Dependency Injection
para acesso ao banco de dados.
"""

from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, Query
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session

from .auth import get_current_user_id
from .database import get_db
from .report_repository import ReportRepository
from .report_service import ReportService

router = APIRouter()


def _get_report_service(db: Session = Depends(get_db)) -> ReportService:
    """
    Factory que cria uma instância de ReportService com suas dependências.

    Args:
        db: Sessão do banco de dados injetada via Depends.

    Returns:
        Instância configurada do ReportService.
    """
    repository = ReportRepository(db)
    return ReportService(repository)


@router.get("/relatorioPorCategoria")
async def gerar_relatorio_por_categoria(
    categoria_id: Optional[int] = Query(None, description="ID da categoria"),
    tipo: Optional[str] = Query(None, description="Tipo: RECEITA ou DESPESA"),
    periodo: Optional[str] = Query(None, description="Período: mensal ou anual"),
    formato: str = Query("excel", description="Formato: excel ou pdf"),
    usuario_id: int = Depends(get_current_user_id),
    service: ReportService = Depends(_get_report_service),
) -> StreamingResponse:
    """
    Gera relatório de transações filtrado por categoria.

    O usuario_id é extraído automaticamente do token JWT.

    Args:
        categoria_id: ID da categoria para filtrar (opcional).
        tipo: Tipo de transação (opcional).
        periodo: Período temporal (opcional).
        formato: Formato de saída — 'excel' ou 'pdf'.
        usuario_id: ID do usuário autenticado (extraído do JWT).
        service: Instância do ReportService (injeção de dependência).

    Returns:
        StreamingResponse com o arquivo do relatório.

    Raises:
        HTTPException 400: Se ocorrer erro na geração do relatório.
    """
    try:
        buffer, mime, filename = await service.gerar_relatorio_por_categoria(
            usuario_id=usuario_id,
            categoria_id=categoria_id,
            tipo=tipo,
            periodo=periodo,
            formato=formato,
        )
        return StreamingResponse(
            buffer,
            media_type=mime,
            headers={"Content-Disposition": f"attachment; filename={filename}"},
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.get("/relatorioSaldoMensal")
async def relatorio_saldo_mensal(
    ano: Optional[int] = Query(None, description="Ano para filtrar (opcional)"),
    conta_id: Optional[int] = Query(
        None, description="ID da conta bancária (opcional)"
    ),
    formato: str = Query("excel", description="Formato: excel ou pdf"),
    usuario_id: int = Depends(get_current_user_id),
    service: ReportService = Depends(_get_report_service),
) -> StreamingResponse:
    """
    Gera relatório de saldo mensal com receitas, despesas e saldo por mês.

    Args:
        ano: Ano para filtrar (opcional).
        conta_id: ID da conta bancária (opcional).
        formato: Formato de saída — 'excel' ou 'pdf'.
        usuario_id: ID do usuário autenticado (extraído do JWT).
        service: Instância do ReportService (injeção de dependência).

    Returns:
        StreamingResponse com o arquivo do relatório.

    Raises:
        HTTPException 400: Se ocorrer erro na geração do relatório.
    """
    try:
        buffer, mime, filename = await service.gerar_relatorio(
            report_type="saldo_mensal",
            format_type=formato,
            usuario_id=usuario_id,
            ano=ano,
            conta_id=conta_id,
        )
        return StreamingResponse(
            buffer,
            media_type=mime,
            headers={"Content-Disposition": f"attachment; filename={filename}"},
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.get("/relatorioTransacaoDetalhado")
async def relatorio_transacao_detalhado(
    conta_id: Optional[int] = Query(
        None, description="ID da conta bancária (opcional)"
    ),
    categoria_id: Optional[int] = Query(
        None, description="ID da categoria (opcional)"
    ),
    data_inicio: Optional[str] = Query(
        None, description="Data de início YYYY-MM-DD (opcional)"
    ),
    data_fim: Optional[str] = Query(
        None, description="Data de fim YYYY-MM-DD (opcional)"
    ),
    valor_minimo: Optional[float] = Query(
        None, description="Valor mínimo (opcional)"
    ),
    valor_maximo: Optional[float] = Query(
        None, description="Valor máximo (opcional)"
    ),
    tipo_transacao: Optional[str] = Query(
        None, description="Tipo: RECEITA ou DESPESA (opcional)"
    ),
    formato: str = Query("excel", description="Formato: excel ou pdf"),
    usuario_id: int = Depends(get_current_user_id),
    service: ReportService = Depends(_get_report_service),
) -> StreamingResponse:
    """
    Gera relatório detalhado de transações com filtros avançados.

    Args:
        conta_id: ID da conta bancária (opcional).
        categoria_id: ID da categoria (opcional).
        data_inicio: Data de início no formato YYYY-MM-DD (opcional).
        data_fim: Data de fim no formato YYYY-MM-DD (opcional).
        valor_minimo: Valor mínimo para filtrar (opcional).
        valor_maximo: Valor máximo para filtrar (opcional).
        tipo_transacao: Tipo de transação (opcional).
        formato: Formato de saída — 'excel' ou 'pdf'.
        usuario_id: ID do usuário autenticado (extraído do JWT).
        service: Instância do ReportService (injeção de dependência).

    Returns:
        StreamingResponse com o arquivo do relatório.

    Raises:
        HTTPException 400: Se ocorrer erro na geração do relatório.
    """
    try:
        buffer, mime, filename = await service.gerar_relatorio(
            report_type="transacoes_detalhadas",
            format_type=formato,
            usuario_id=usuario_id,
            conta_id=conta_id,
            categoria_id=categoria_id,
            data_inicio=data_inicio,
            data_fim=data_fim,
            valor_minimo=valor_minimo,
            valor_maximo=valor_maximo,
            tipo_transacao=tipo_transacao,
        )
        return StreamingResponse(
            buffer,
            media_type=mime,
            headers={"Content-Disposition": f"attachment; filename={filename}"},
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))