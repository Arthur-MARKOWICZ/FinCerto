from io import BytesIO
from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
import pandas as pd
from .report_repository import ReportRepository

class ReportService:
    def __init__(self, report_repository: ReportRepository):
        self.report_repository = report_repository

    async def gerar_relatorio(self, usuario_id, categoria_id=None, tipo=None, periodo=None, formato="excel"):

        dados = self.report_repository.buscar_relatorio(usuario_id, categoria_id, tipo, periodo)
        if not dados:
            raise ValueError("Nenhuma transação encontrada para os filtros fornecidos.")


        if formato.lower() == "excel":
            return self._gerar_excel(dados)
        elif formato.lower() == "pdf":
            return self._gerar_pdf(dados)
        else:
            raise ValueError("Formato inválido. Use 'excel' ou 'pdf'.")

    def _tuple_to_dict(self, row):

        return {
            'data': row[0],
            'descricao': row[1],
            'categoria': row[2],
            'valor': row[3],
            'tipo': row[4]
        }

    def _gerar_excel(self, dados):
  
        dados_dicts = [self._tuple_to_dict(d) for d in dados]
        df = pd.DataFrame(dados_dicts)
        buffer = BytesIO()
        with pd.ExcelWriter(buffer, engine="openpyxl") as writer:
            df.to_excel(writer, index=False, sheet_name="Relatório")
        buffer.seek(0)
        return buffer, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "relatorio.xlsx"

    def _gerar_pdf(self, dados):
        buffer = BytesIO()
        pdf = canvas.Canvas(buffer, pagesize=A4)
        
        # Configurações iniciais
        width, height = A4
        margin = 50
        line_height = 20
        current_y = height - margin - 40
        
        # Cabeçalho
        try:
            pdf.setFont("Helvetica-Bold", 16)
        except:
            pdf.setFont("Helvetica-Bold", 14)
        pdf.drawCentredString(width/2, height - margin, "Relatório Financeiro")
        
        # Data de emissão
        from datetime import datetime
        try:
            pdf.setFont("Helvetica", 10)
        except:
            pdf.setFont("Helvetica", 10)
        pdf.drawString(margin, height - margin - 20, f"Emitido em: {datetime.now().strftime('%d/%m/%Y %H:%M')}")
        
        # Tabela - Cabeçalho
        current_y -= 40
        try:
            pdf.setFont("Helvetica-Bold", 10)
        except:
            pdf.setFont("Helvetica-Bold", 10)
        headers = ["Data", "Descrição", "Categoria", "Valor", "Tipo"]
        col_widths = [80, 150, 100, 80, 60]
        
        # Desenha cabeçalho
        x = margin
        for i, header in enumerate(headers):
            pdf.drawString(x, current_y, header)
            x += col_widths[i]
        
        # Linha de separação
        current_y -= 10
        pdf.line(margin, current_y, width - margin, current_y)
        current_y -= 15
        
        # Dados
        pdf.setFont("Helvetica", 9)
        total = 0
        
        for d in dados:
            d_dict = self._tuple_to_dict(d)
            
            # Formata a data
            data = d_dict['data'].strftime('%d/%m/%Y') if hasattr(d_dict['data'], 'strftime') else str(d_dict['data'])
            
            # Formata o valor
            valor = float(d_dict['valor'])
            valor_formatado = f"R$ {valor:,.2f}".replace('.', '|').replace(',', '.').replace('|', ',')
            total += valor
            
            # Quebra de linha se necessário
            if current_y < 100:
                pdf.showPage()
                current_y = height - margin - 20
                pdf.setFont("Helvetica", 9)
            
            # Desenha a linha de dados
            x = margin
            col_values = [
                data,
                d_dict['descricao'],
                d_dict['categoria'],
                valor_formatado,
                d_dict['tipo']
            ]
            
            # Ajusta o tamanho do texto para caber na célula
            for i, value in enumerate(col_values):
                if i == 1:  # Descrição
                    # Quebra a descrição em várias linhas se for muito longa
                    text = pdf.beginText(x, current_y)
                    try:
                        text.setFont("Helvetica", 9)
                    except:
                        text.setFont("Helvetica", 9)
                    text.textLine(str(value)[:25])  # Limita a 25 caracteres por linha
                    pdf.drawText(text)
                else:
                    try:
                        pdf.setFont("Helvetica", 9)
                    except:
                        pdf.setFont("Helvetica", 9)
                    pdf.drawString(x + 2, current_y - 10, str(value)[:20])  # Limita o tamanho do texto
                x += col_widths[i]
            
            current_y -= line_height
        
        # Linha de total
        current_y -= 20
        pdf.line(margin, current_y, width - margin, current_y)
        current_y -= 15
        
        # Total
        pdf.setFont("Helvetica-Bold", 10)
        total_formatado = f"R$ {total:,.2f}".replace('.', '|').replace(',', '.').replace('|', ',')
        pdf.drawString(width - margin - 80, current_y, f"Total: {total_formatado}")
        
        # Rodapé
        try:
            pdf.setFont("Helvetica-Italic", 8)
        except:
            pdf.setFont("Helvetica", 8)
        pdf.drawCentredString(width/2, 30, "Sistema FinanCerto - Relatório Gerado Automaticamente")
        
        pdf.save()
        buffer.seek(0)
        return buffer, "application/pdf", "relatorio_financeiro.pdf"