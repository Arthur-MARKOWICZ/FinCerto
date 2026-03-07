"""
Pydantic v2 schemas para validação de entrada e saída da API de Analytics.

Schemas organizam e validam os parâmetros recebidos nos endpoints,
garantindo contratos de dados seguros entre client e server.
"""

from typing import Optional
from pydantic import BaseModel, Field


class RelatorioPorCategoriaParams(BaseModel):
    """Parâmetros para geração de relatório por categoria."""

    usuario_id: int = Field(..., description="ID do usuário autenticado")
    categoria_id: Optional[int] = Field(None, description="ID da categoria para filtrar")
    tipo: Optional[str] = Field(None, description="Tipo de transação (RECEITA ou DESPESA)")
    periodo: Optional[str] = Field(None, description="Período: 'mensal' ou 'anual'")
    formato: str = Field("excel", description="Formato do relatório: 'excel' ou 'pdf'")


class RelatorioSaldoMensalParams(BaseModel):
    """Parâmetros para geração de relatório de saldo mensal."""

    usuario_id: int = Field(..., description="ID do usuário autenticado")
    ano: Optional[int] = Field(None, description="Ano para filtrar (opcional)")
    conta_id: Optional[int] = Field(None, description="ID da conta bancária (opcional)")
    formato: str = Field("excel", description="Formato do relatório: 'excel' ou 'pdf'")


class RelatorioTransacaoDetalhadoParams(BaseModel):
    """Parâmetros para geração de relatório de transações detalhadas."""

    usuario_id: int = Field(..., description="ID do usuário autenticado")
    conta_id: Optional[int] = Field(None, description="ID da conta bancária (opcional)")
    categoria_id: Optional[int] = Field(None, description="ID da categoria (opcional)")
    data_inicio: Optional[str] = Field(None, description="Data de início (YYYY-MM-DD)")
    data_fim: Optional[str] = Field(None, description="Data de fim (YYYY-MM-DD)")
    valor_minimo: Optional[float] = Field(None, description="Valor mínimo para filtrar")
    valor_maximo: Optional[float] = Field(None, description="Valor máximo para filtrar")
    tipo_transacao: Optional[str] = Field(None, description="Tipo de transação (RECEITA ou DESPESA)")
    formato: str = Field("excel", description="Formato do relatório: 'excel' ou 'pdf'")


class MessageResponse(BaseModel):
    """Resposta genérica da API."""

    message: str


class DBTimeResponse(BaseModel):
    """Resposta do teste de conexão com o banco de dados."""

    db_time: str
