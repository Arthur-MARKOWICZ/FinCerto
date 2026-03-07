"""
Repositório de dados para geração de relatórios.

Responsável por executar queries read-only no banco de dados
compartilhado com o serviço Java (finanCertoBack).
As tabelas são gerenciadas pelo Spring Boot/JPA — este serviço
apenas lê os dados.
"""

from datetime import date
from typing import Any, Dict, List, Optional

from sqlalchemy.orm import Session
from sqlalchemy.sql import text


class ReportRepository:
    """
    Repositório read-only para consulta de dados financeiros.

    Executa queries SQL parametrizadas nas tabelas gerenciadas
    pelo Spring Boot (tb_transacao, tb_categoria, tb_conta, tb_usuario).
    """

    def __init__(self, db: Session) -> None:
        self.session = db

    def buscar_relatorio(
        self,
        usuario_id: int,
        categoria_id: Optional[int] = None,
        tipo: Optional[str] = None,
        periodo: Optional[str] = None,
        conta_id: Optional[int] = None,
        data_inicio: Optional[str] = None,
        data_fim: Optional[str] = None,
        valor_minimo: Optional[float] = None,
        valor_maximo: Optional[float] = None,
    ) -> List[Any]:
        """
        Busca transações filtradas para relatório por categoria.

        Args:
            usuario_id: ID do usuário proprietário das transações.
            categoria_id: Filtrar por categoria específica (opcional).
            tipo: Tipo de transação — 'RECEITA' ou 'DESPESA' (opcional).
            periodo: Período temporal — 'mensal' ou 'anual' (opcional).
            conta_id: Filtrar por conta bancária (opcional).
            data_inicio: Data inicial no formato YYYY-MM-DD (opcional).
            data_fim: Data final no formato YYYY-MM-DD (opcional).
            valor_minimo: Valor mínimo para filtrar (opcional).
            valor_maximo: Valor máximo para filtrar (opcional).

        Returns:
            Lista de registros (Row) com data, descricao, categoria,
            conta, valor e tipo.
        """
        sql = text("""
            SELECT
                t.date as data,
                t.descricao,
                c.nome AS categoria,
                co.nome as conta,
                t.valor,
                t.tipo
            FROM tb_transacao t
            JOIN tb_categoria c ON c.id = t.categoria_id
            LEFT JOIN tb_conta co ON t.conta_id = co.id
            WHERE t.usuario_id = :usuario_id
              AND (:categoria_id IS NULL OR t.categoria_id = :categoria_id)
              AND (:tipo IS NULL OR t.tipo = :tipo)
              AND (:conta_id IS NULL OR t.conta_id = :conta_id)
              AND (:data_inicio IS NULL OR t.date >= :data_inicio)
              AND (:data_fim IS NULL OR t.date <= :data_fim)
              AND (:valor_minimo IS NULL OR t.valor >= :valor_minimo)
              AND (:valor_maximo IS NULL OR t.valor <= :valor_maximo)
              AND (
                    (:periodo = 'mensal'
                     AND DATE_TRUNC('month', t.date) = DATE_TRUNC('month', CURRENT_DATE))
                    OR (:periodo = 'anual'
                        AND DATE_TRUNC('year', t.date) = DATE_TRUNC('year', CURRENT_DATE))
                    OR (:periodo IS NULL)
                  )
            ORDER BY t.date DESC
        """)

        params = {
            "usuario_id": usuario_id,
            "categoria_id": categoria_id,
            "tipo": tipo,
            "periodo": periodo,
            "conta_id": conta_id,
            "data_inicio": data_inicio,
            "data_fim": data_fim,
            "valor_minimo": valor_minimo,
            "valor_maximo": valor_maximo,
        }

        return self.session.execute(sql, params).fetchall()

    def buscar_transacoes_detalhadas(
        self,
        usuario_id: int,
        conta_id: Optional[int] = None,
        categoria_id: Optional[int] = None,
        data_inicio: Optional[date] = None,
        data_fim: Optional[date] = None,
        valor_minimo: Optional[float] = None,
        valor_maximo: Optional[float] = None,
        tipo_transacao: Optional[str] = None,
    ) -> List[Dict[str, Any]]:
        """
        Busca transações detalhadas com filtros avançados.

        Args:
            usuario_id: ID do usuário proprietário das transações.
            conta_id: Filtrar por conta bancária (opcional).
            categoria_id: Filtrar por categoria (opcional).
            data_inicio: Data inicial para filtrar (opcional).
            data_fim: Data final para filtrar (opcional).
            valor_minimo: Valor mínimo para filtrar (opcional).
            valor_maximo: Valor máximo para filtrar (opcional).
            tipo_transacao: 'RECEITA' ou 'DESPESA' (opcional).

        Returns:
            Lista de dicionários contendo id, data, data_formatada,
            descricao, valor, tipo_transacao, categoria, conta e tipo_conta.
        """
        sql = text("""
            SELECT
                t.id,
                t.date as data,
                t.descricao,
                t.valor,
                t.tipo as tipo_transacao,
                c.nome as categoria,
                co.nome as conta,
                co.tipo as tipo_conta
            FROM tb_transacao t
            LEFT JOIN tb_categoria c ON t.categoria_id = c.id
            LEFT JOIN tb_conta co ON t.conta_id = co.id
            WHERE t.usuario_id = :usuario_id
              AND (:conta_id IS NULL OR t.conta_id = :conta_id)
              AND (:categoria_id IS NULL OR t.categoria_id = :categoria_id)
              AND (:data_inicio IS NULL OR t.date >= :data_inicio)
              AND (:data_fim IS NULL OR t.date <= :data_fim)
              AND (:valor_minimo IS NULL OR t.valor >= :valor_minimo)
              AND (:valor_maximo IS NULL OR t.valor <= :valor_maximo)
              AND (:tipo_transacao IS NULL OR t.tipo = :tipo_transacao)
            ORDER BY t.date DESC, t.id
        """)

        params = {
            "usuario_id": usuario_id,
            "conta_id": conta_id,
            "categoria_id": categoria_id,
            "data_inicio": data_inicio,
            "data_fim": data_fim,
            "valor_minimo": valor_minimo,
            "valor_maximo": valor_maximo,
            "tipo_transacao": tipo_transacao,
        }

        result = self.session.execute(sql, params).fetchall()

        return [
            {
                "id": row[0],
                "data": row[1],
                "data_formatada": row[1].strftime("%d/%m/%Y"),
                "descricao": row[2],
                "valor": float(row[3]) if row[3] else 0.0,
                "tipo_transacao": row[4],
                "categoria": row[5] if row[5] else "Sem Categoria",
                "conta": row[6] if row[6] else "Sem Conta",
                "tipo_conta": row[7] if row[7] else "OUTRO",
            }
            for row in result
        ]

    def buscar_saldo_mensal(
        self,
        usuario_id: int,
        ano: Optional[int] = None,
        conta_id: Optional[int] = None,
    ) -> List[Dict[str, Any]]:
        """
        Busca o saldo mensal (receitas - despesas) agrupado por mês.

        Args:
            usuario_id: ID do usuário proprietário das transações.
            ano: Ano para filtrar (opcional).
            conta_id: ID da conta bancária para filtrar (opcional).

        Returns:
            Lista de dicionários com mes, mes_nome, receitas,
            despesas e saldo de cada mês.
        """
        # Verificar existência do usuário
        check_user = text("SELECT id FROM tb_usuario WHERE id = :usuario_id")
        user_exists = self.session.execute(
            check_user, {"usuario_id": usuario_id}
        ).fetchone()

        if not user_exists:
            return []

        # Verificar se existem transações do tipo relevante
        check_sql = text("""
            SELECT COUNT(*) as total
            FROM tb_transacao
            WHERE usuario_id = :usuario_id
                AND tipo IN ('RECEITA', 'DESPESA')
        """)
        total_transacoes: int = self.session.execute(
            check_sql, {"usuario_id": usuario_id}
        ).scalar()

        if total_transacoes == 0:
            return []

        # Query principal de agregação mensal
        sql = text("""
            SELECT
                DATE_TRUNC('month', t.date) AS mes,
                COALESCE(SUM(CASE WHEN t.tipo = 'RECEITA'
                    THEN t.valor ELSE 0 END), 0) as receitas,
                COALESCE(SUM(CASE WHEN t.tipo = 'DESPESA'
                    THEN t.valor ELSE 0 END), 0) as despesas,
                COALESCE(SUM(CASE WHEN t.tipo = 'RECEITA'
                    THEN t.valor ELSE -t.valor END), 0) as saldo
            FROM tb_transacao t
            WHERE t.usuario_id = :usuario_id
              AND (:ano IS NULL OR EXTRACT(YEAR FROM t.date) = :ano)
              AND (:conta_id IS NULL OR t.conta_id = :conta_id)
              AND t.tipo IN ('RECEITA', 'DESPESA')
            GROUP BY DATE_TRUNC('month', t.date)
            ORDER BY mes
        """)

        params = {
            "usuario_id": usuario_id,
            "ano": ano,
            "conta_id": conta_id,
        }

        result = self.session.execute(sql, params).fetchall()

        return [
            {
                "mes": row[0].strftime("%Y-%m"),
                "mes_nome": row[0].strftime("%B/%Y").capitalize(),
                "receitas": float(row[1]) if row[1] else 0.0,
                "despesas": float(row[2]) if row[2] else 0.0,
                "saldo": float(row[3]) if row[3] else 0.0,
            }
            for row in result
        ]