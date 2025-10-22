from sqlalchemy.sql import text

class ReportRepository:
    def __init__(self, db: Session):
         self.session = db

    def buscar_relatorio(self, usuario_id, categoria_id=None, tipo=None, periodo=None):
        sql = text("""
            SELECT 
                t.date as data,
                t.descricao,
                c.nome AS categoria,
                t.valor,
                t.tipo
            FROM tb_transacao t
            JOIN tb_categoria c ON c.id = t.categoria_id
            WHERE t.usuario_id = :usuarioId
              AND (:categoriaId IS NULL OR t.categoria_id = :categoriaId)
              AND (:tipo IS NULL OR t.tipo = :tipo)
              AND (
                    (:periodo = 'mensal' AND DATE_TRUNC('month', t.date) = DATE_TRUNC('month', CURRENT_DATE))
                    OR (:periodo = 'anual' AND DATE_TRUNC('year', t.date) = DATE_TRUNC('year', CURRENT_DATE))
                    OR (:periodo IS NULL)
                  )
            ORDER BY t.date DESC
        """)
        params = {
            "usuarioId": usuario_id,
            "categoriaId": categoria_id,
            "tipo": tipo,
            "periodo": periodo
        }
        return self.session.execute(sql, params).fetchall()