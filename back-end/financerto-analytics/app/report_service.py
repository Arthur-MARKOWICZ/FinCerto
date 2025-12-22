from abc import ABC, abstractmethod
from io import BytesIO
from typing import List, Dict, Any, Tuple, Union
import pandas as pd
from datetime import datetime
from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
from .report_repository import ReportRepository

class ReportGenerator(ABC):
    """Classe abstrata para geradores de relatório"""
    
    @abstractmethod
    def generate(self, data: List[Any], **kwargs) -> Tuple[BytesIO, str, str]:
        """Gera o relatório nos formatos suportados"""
        pass

class ExcelGenerator(ReportGenerator):
    """Gera relatórios em formato Excel"""
    
    def __init__(self, columns: List[Dict[str, str]], sheet_name: str = "Relatório"):
        self.columns = columns
        self.sheet_name = sheet_name
    
    def _format_data(self, data: List[Any]) -> List[Dict[str, Any]]:
        """Formata os dados para o formato do DataFrame"""
        return [self._row_to_dict(row) for row in data]
    
    def _row_to_dict(self, row: Any) -> Dict[str, Any]:
        """Converte uma linha de dados para dicionário"""
        if isinstance(row, dict):
            return row
        return {}
    
    def generate(self, data: List[Any], **kwargs) -> Tuple[BytesIO, str, str]:
        """Gera o relatório em Excel"""
        if not data:
            raise ValueError("Nenhum dado fornecido para gerar o relatório")
            
        df = pd.DataFrame(self._format_data(data))
        buffer = BytesIO()
        
        with pd.ExcelWriter(buffer, engine="openpyxl") as writer:
            df.to_excel(
                writer, 
                index=False, 
                sheet_name=self.sheet_name,
                columns=[col['field'] for col in self.columns]
            )
            
        buffer.seek(0)
        filename = f"relatorio_{datetime.now().strftime('%Y%m%d_%H%M%S')}.xlsx"
        return buffer, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename

class PDFGenerator(ReportGenerator):
    """Gera relatórios em formato PDF"""
    
    def __init__(self, 
                 title: str, 
                 columns: List[Dict[str, str]],
                 page_size=A4,
                 margin: int = 50,
                 line_height: int = 20):
        self.title = title
        self.columns = columns
        self.page_size = page_size
        self.margin = margin
        self.line_height = line_height
    
    def _draw_header(self, pdf, width: float, height: float) -> float:
        """Desenha o cabeçalho do relatório"""
        try:
            pdf.setFont("Helvetica-Bold", 16)
        except:
            pdf.setFont("Helvetica-Bold", 14)
            
        # Título
        pdf.drawCentredString(width/2, height - self.margin, self.title)
        
        # Data de emissão
        try:
            pdf.setFont("Helvetica", 10)
        except:
            pdf.setFont("Helvetica", 10)
            
        pdf.drawString(
            self.margin, 
            height - self.margin - 20, 
            f"Emitido em: {datetime.now().strftime('%d/%m/%Y %H:%M')}"
        )
        
        return height - self.margin - 40
    
    def _draw_table_headers(self, pdf, y_pos: float) -> float:
        """Desenha os cabeçalhos da tabela"""
        try:
            pdf.setFont("Helvetica-Bold", 11)
        except:
            pdf.setFont("Helvetica-Bold", 11)
            
        x = self.margin
        for col in self.columns:
            pdf.drawString(x, y_pos, col['header'])
            x += col.get('width', 100)
            
        return y_pos - 10
    
    def _draw_footer(self, pdf, width: float, y_pos: float):
        """Desenha o rodapé do relatório"""
        try:
            pdf.setFont("Helvetica-Italic", 8)
        except:
            pdf.setFont("Helvetica", 8)
        pdf.drawCentredString(
            width/2, 
            30, 
            "Sistema FinanCerto - Relatório Gerado Automaticamente"
        )
    
    def generate(self, data: List[Any], **kwargs) -> Tuple[BytesIO, str, str]:
        """Gera o relatório em PDF"""
        if not data:
            raise ValueError("Nenhum dado fornecido para gerar o relatório")
            
        buffer = BytesIO()
        width, height = self.page_size
        pdf = canvas.Canvas(buffer, pagesize=self.page_size)
        
        current_y = self._draw_header(pdf, width, height)
        current_y = self._draw_table_headers(pdf, current_y)
        
        pdf.line(self.margin, current_y, width - self.margin, current_y)
        current_y -= 15
        
        # Configuração da fonte
        try:
            pdf.setFont("Helvetica", 9)
        except:
            pdf.setFont("Helvetica", 9)
        
        # Desenha as linhas de dados
        for row in data:
            if current_y < 100:  # Verifica se precisa de nova página
                pdf.showPage()
                current_y = height - self.margin - 20
                self._draw_table_headers(pdf, current_y)
                current_y -= 25
                try:
                    pdf.setFont("Helvetica", 9)
                except:
                    pdf.setFont("Helvetica", 9)
            
            x = self.margin
            for col in self.columns:
                # Obtém o valor do dicionário ou do atributo do objeto
                if isinstance(row, dict):
                    value = row.get(col['field'], '')
                else:
                    value = getattr(row, col['field'], '')
                
                # Formata o valor conforme necessário
                if isinstance(value, datetime):
                    value = value.strftime('%d/%m/%Y')
                elif isinstance(value, (int, float)) and col.get('format') == 'currency':
                    value = f"R$ {value:,.2f}".replace('.', '|').replace(',', '.').replace('|', ',')
                
                # Desenha o texto na posição correta
                if col.get('multiline'):
                    text = pdf.beginText(x, current_y)
                    text.textLine(str(value)[:25])
                    pdf.drawText(text)
                else:
                    # Ajusta a posição Y para centralizar verticalmente o texto na célula
                    text_y = current_y - 10
                    pdf.drawString(x + 2, text_y, str(value)[:30])  # Aumentei o limite de caracteres para 30
                
                x += col.get('width', 100)
            
            current_y -= self.line_height
        
        # Desenha o rodapé na última página
        self._draw_footer(pdf, width, 30)
        
        pdf.save()
        buffer.seek(0)
        filename = f"relatorio_{datetime.now().strftime('%Y%m%d_%H%M%S')}.pdf"
        return buffer, "application/pdf", filename

class ReportService:
    """Serviço para geração de relatórios"""
    
    # Definição dos relatórios disponíveis
    REPORTS = {
        'transacoes_por_categoria': {
            'title': 'Relatório de Transações por Categoria',
            'columns': [
                {'field': 'data', 'header': 'Data', 'width': 80, 'format': 'date'},
                {'field': 'descricao', 'header': 'Descrição', 'width': 150, 'multiline': True},
                {'field': 'categoria', 'header': 'Categoria', 'width': 100},
                {'field': 'valor', 'header': 'Valor', 'width': 80, 'format': 'currency'},
                {'field': 'tipo', 'header': 'Tipo', 'width': 60}
            ]
        },
        'saldo_mensal': {
            'title': 'Relatório de Saldo Mensal',
            'columns': [
                {'field': 'mes_nome', 'header': 'Mês/Ano', 'width': 120},
                {'field': 'receitas', 'header': 'Receitas (R$)', 'width': 100, 'format': 'currency'},
                {'field': 'despesas', 'header': 'Despesas (R$)', 'width': 100, 'format': 'currency'},
                {'field': 'saldo', 'header': 'Saldo (R$)', 'width': 100, 'format': 'currency'}
            ]
        },
        'transacoes_detalhadas': {
            'title': 'Relatório de Transações Detalhadas',
            'columns': [
                {'field': 'data_formatada', 'header': 'Data', 'width': 350},
                {'field': 'descricao', 'header': 'Descrição', 'width': 350, 'multiline': True},
                {'field': 'categoria', 'header': 'Categoria', 'width': 350},
                {'field': 'conta', 'header': 'Conta', 'width': 350},
                {'field': 'tipo_transacao', 'header': 'Tipo', 'width': 350},
                {'field': 'valor', 'header': 'Valor (R$)', 'width': 350, 'format': 'currency'}
            ]
        }
        
    }
    
    def __init__(self, report_repository: ReportRepository):
        self.report_repository = report_repository
    
    def _get_generator(self, report_type: str, format_type: str) -> ReportGenerator:
        """Obtém o gerador apropriado para o tipo de relatório e formato"""
        if report_type not in self.REPORTS:
            raise ValueError(f"Tipo de relatório inválido: {report_type}")
            
        report_config = self.REPORTS[report_type]
        
        if format_type.lower() == 'excel':
            return ExcelGenerator(
                columns=report_config['columns'],
                sheet_name=report_config['title'][:31]  # Limite de caracteres para nome da planilha
            )
        elif format_type.lower() == 'pdf':
            return PDFGenerator(
                title=report_config['title'],
                columns=report_config['columns']
            )
        else:
            raise ValueError(f"Formato de saída inválido: {format_type}")
    
    async def gerar_relatorio(self, 
                           report_type: str,
                           format_type: str = 'excel',
                           **filters) -> Tuple[BytesIO, str, str]:
        """
        Gera um relatório no formato especificado
        
        Args:
            report_type: Tipo de relatório (ex: 'transacoes_por_categoria')
            format_type: Formato de saída ('excel' ou 'pdf')
            **filters: Filtros para a consulta do relatório
            
        Returns:
            Tuple[BytesIO, str, str]: Buffer com o relatório, tipo MIME e nome do arquivo
        """
        if report_type == 'saldo_mensal':
            data = self.report_repository.buscar_saldo_mensal(
                usuario_id=filters.get('usuario_id'),
                ano=filters.get('ano'),
                conta_id=filters.get('conta_id')
            )
        elif report_type == 'transacoes_detalhadas':
            data = self.report_repository.buscar_transacoes_detalhadas(
                usuario_id=filters.get('usuario_id'),
                conta_id=filters.get('conta_id'),
                categoria_id=filters.get('categoria_id'),
                data_inicio=filters.get('data_inicio'),
                data_fim=filters.get('data_fim'),
                valor_minimo=filters.get('valor_minimo'),
                valor_maximo=filters.get('valor_maximo'),
                tipo_transacao=filters.get('tipo_transacao')
            )
        else:
            data = self.report_repository.buscar_relatorio(**filters)
        
        
        
        if not data:
            raise ValueError("Nenhum dado encontrado para os filtros fornecidos.")
        
        # Obtém o gerador apropriado
        generator = self._get_generator(report_type, format_type)
        
        # Gera o relatório
        return generator.generate(data)
    
    # Método de compatibilidade com o código existente
    async def gerar_relatorio_por_categoria(self, usuario_id, categoria_id=None, 
                                         tipo=None, periodo=None, formato="excel"):
        return await self.gerar_relatorio(
            report_type='transacoes_por_categoria',
            format_type=formato,
            usuario_id=usuario_id,
            categoria_id=categoria_id,
            tipo=tipo,
            periodo=periodo
        )
        