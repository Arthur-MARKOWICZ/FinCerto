from abc import ABC, abstractmethod
from io import BytesIO
from typing import List, Dict, Any, Tuple, Union, Optional
import pandas as pd
from datetime import datetime
from reportlab.lib.pagesizes import A4, letter
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, PageBreak
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.lib.colors import HexColor
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_RIGHT
import logging
from .report_repository import ReportRepository


logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class ReportGenerator(ABC):
   
    
    @abstractmethod
    def generate(self, data: List[Any], **kwargs) -> Tuple[BytesIO, str, str]:
       
        pass
    
    def validate_data(self, data: List[Any]) -> None:
       
        if data is None:
            raise ValueError("Dados não podem ser None")
        if not isinstance(data, (list, tuple)):
            raise TypeError(f"Os dados devem ser uma lista ou tupla, recebido: {type(data).__name__}")
        if len(data) == 0:
            raise ValueError("Nenhum dado fornecido para gerar o relatório")
        
        logger.debug(f"Validação de dados passou. Total de registros: {len(data)}")
    
    def _safe_get_value(self, obj: Any, field: str, default: str = "") -> str:
      
        try:
            if isinstance(obj, dict):
                return str(obj.get(field, default))
            elif hasattr(obj, field):
                return str(getattr(obj, field, default))
            return default
        except Exception as e:
            logger.warning(f"Erro ao extrair campo '{field}': {e}")
            return default

class ExcelGenerator(ReportGenerator):
  
    def __init__(self, columns: List[Dict[str, str]], sheet_name: str = "Relatório", title: str = None):
        self.columns = columns
        self.sheet_name = sheet_name
        self.title = title
        self.logger = logging.getLogger(self.__class__.__name__)
    
    def _format_data(self, data: List[Any]) -> List[Dict[str, Any]]:
        
        try:
            formatted_data = []
            for i, row in enumerate(data):
                try:
                    row_dict = self._row_to_dict(row)
                   
                    if not row_dict:
                        self.logger.warning(f"Linha {i} veio vazia ou não pôde ser convertida: {row}")
                        continue
                    
                    if 'data' not in row_dict:
                        row_dict['data'] = ''
                    if 'descricao' not in row_dict:
                        row_dict['descricao'] = ''
                    if 'categoria' not in row_dict:
                        row_dict['categoria'] = ''
                    if 'valor' not in row_dict:
                        row_dict['valor'] = 0.0
                    if 'tipo' not in row_dict:
                        row_dict['tipo'] = ''
                    
                    formatted_data.append(row_dict)
                except Exception as e:
                    self.logger.error(f"Erro ao formatar linha {i}: {e}, dados: {row}")
                    
                    formatted_data.append({
                        'data': '',
                        'descricao': '',
                        'categoria': '',
                        'valor': 0.0,
                        'tipo': ''
                    })
            
            if not formatted_data:
                self.logger.warning("Nenhum dado foi formatado corretamente")
              
                return []
            
            return formatted_data
        except Exception as e:
            self.logger.error(f"Erro ao formatar dados: {e}")
            raise
    
    def _row_to_dict(self, row: Any) -> Dict[str, Any]:
  
        self.logger.debug(f"Tentando converter: {type(row)} - {row}")
        
        if isinstance(row, dict):
            return row
        elif isinstance(row, (tuple, list)) or 'sqlalchemy.engine.row.Row' in str(type(row)):
      
            try:
               
                if len(row) >= 6: 
                    result = {
                        'data': row[0] if row[0] is not None else '',
                        'descricao': row[1] if row[1] is not None else '',
                        'categoria': row[2] if row[2] is not None else '',
                        'valor': float(row[4]) if row[4] is not None else 0.0,
                        'tipo': str(row[5]) if row[5] is not None else ''
                    }
                    self.logger.debug(f"Conversão bem sucedida: {result}")
                    return result
                else:
                    self.logger.warning(f"Registro com tamanho insuficiente: {row} (esperado >=6, got {len(row)})")
            except (IndexError, ValueError, TypeError) as e:
                self.logger.error(f"Erro ao converter registro {row}: {e}")
        elif hasattr(row, '__dict__'):
          
            try:
                return {k: v for k, v in row.__dict__.items() if not k.startswith('_')}
            except Exception as e:
                self.logger.error(f"Erro ao converter objeto com __dict__: {e}")
        else:
            self.logger.warning(f"Tipo não suportado: {type(row)} - {row}")
        return {}
    
    def _format_excel_with_styling(self, df: pd.DataFrame, writer, workbook):
     
        try:
            worksheet = writer.sheets[self.sheet_name]
            
    
            header_format = workbook.add_format({
                'bold': True,
                'bg_color': '#4472C4',
                'font_color': 'white',
                'border': 1,
                'align': 'center',
                'valign': 'vcenter'
            })
            
           
            data_format = workbook.add_format({
                'border': 1,
                'align': 'left',
                'valign': 'vcenter'
            })
            
         
            for col_num, col in enumerate(df.columns):
                worksheet.write(0, col_num, col, header_format)
                worksheet.set_column(col_num, col_num, 18)
            
           
            worksheet.set_row(0, 25)
            
        except Exception as e:
            self.logger.warning(f"Erro ao aplicar formatação Excel: {e}")
    
    def generate(self, data: List[Any], **kwargs) -> Tuple[BytesIO, str, str]:

        try:
            self.validate_data(data)
            
            formatted_data = self._format_data(data)
            
     
            if not formatted_data:
                self.logger.warning("Dados formatados está vazio, criando DataFrame com colunas padrão")

                df = pd.DataFrame(columns=[col['field'] for col in self.columns])
            else:
                df = pd.DataFrame(formatted_data)
      
            available_columns = [col['field'] for col in self.columns if col['field'] in df.columns]
            missing_columns = [col['field'] for col in self.columns if col['field'] not in df.columns]
            
            if missing_columns:
                self.logger.warning(f"Colunas ausentes no DataFrame: {missing_columns}")

                for col in missing_columns:
                    df[col] = ""
            
            buffer = BytesIO()
            
            with pd.ExcelWriter(buffer, engine="openpyxl") as writer:
                df.to_excel(
                    writer, 
                    index=False, 
                    sheet_name=self.sheet_name,
                    columns=available_columns
                )
                
           
                try:
                    from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
                    ws = writer.sheets[self.sheet_name]
                    
               
                    fill = PatternFill(start_color="4472C4", end_color="4472C4", fill_type="solid")
                    font = Font(bold=True, color="FFFFFF")
                    alignment = Alignment(horizontal="center", vertical="center")
                    
                    for cell in ws[1]:
                        cell.fill = fill
                        cell.font = font
                        cell.alignment = alignment
                except:
                    pass
            
            buffer.seek(0)
            timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
            filename = f"relatorio_{timestamp}.xlsx"
            
            self.logger.info(f"Relatório Excel gerado: {filename}")
            return buffer, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename
        
        except Exception as e:
            self.logger.error(f"Erro ao gerar Excel: {e}")
            raise

class PDFGenerator(ReportGenerator):
    
    
    def __init__(self, title: str, columns: List[Dict[str, str]]):
        self.title = title
        self.columns = columns
        self.logger = logging.getLogger(self.__class__.__name__)
    
    def _build_table_data(self, data: List[Any]) -> List[List[str]]:
      
        try:
            table_data = []
       
            header = [col.get('header', col['field']) for col in self.columns]
            table_data.append(header)

            for row in data:
                table_row = []
                for col in self.columns:
                    field = col['field']
                    
                  
                    if isinstance(row, dict):
                        value = row.get(field, '')
                    else:
                        value = getattr(row, field, '')
                    
                  
                    if isinstance(value, datetime):
                        value = value.strftime('%d/%m/%Y')
                    elif isinstance(value, (int, float)):
                        if col.get('format') == 'currency':
                            value = f"R$ {value:,.2f}".replace('.', '|').replace(',', '.').replace('|', ',')
                        else:
                            value = f"{value:,.2f}".replace('.', '|').replace(',', '.').replace('|', ',')
                  
                    value_str = str(value)
                    if len(value_str) > 40:
                        value_str = value_str[:37] + "..."
                    
                    table_row.append(value_str)
                
                table_data.append(table_row)
            
            return table_data
        except Exception as e:
            self.logger.error(f"Erro ao construir dados da tabela: {e}")
            return []
    
    def _create_styled_table(self, table_data: List[List[str]]) -> Table:
        
        table = Table(table_data, repeatRows=1, colWidths=[100]*len(self.columns))
        
     
        header_bg = HexColor('#2C3E50')
        header_text = colors.whitesmoke
        row_bg_light = HexColor('#ECF0F1')
        row_bg_dark = HexColor('#FFFFFF')
        border_color = HexColor('#BDC3C7')
        
        style_commands = [

            ('BACKGROUND', (0, 0), (-1, 0), header_bg),
            ('TEXTCOLOR', (0, 0), (-1, 0), header_text),
            ('ALIGN', (0, 0), (-1, 0), 'CENTER'),
            ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
            ('FONTSIZE', (0, 0), (-1, 0), 11),
            ('BOTTOMPADDING', (0, 0), (-1, 0), 14),
            ('TOPPADDING', (0, 0), (-1, 0), 14),
            
      
            ('ROWBACKGROUNDS', (0, 1), (-1, -1), [row_bg_dark, row_bg_light]),
            ('TEXTCOLOR', (0, 1), (-1, -1), HexColor('#2C3E50')),
            ('ALIGN', (0, 1), (-1, -1), 'RIGHT'),
            ('ALIGN', (0, 1), (0, -1), 'LEFT'), 
            ('FONTNAME', (0, 1), (-1, -1), 'Helvetica'),
            ('FONTSIZE', (0, 1), (-1, -1), 10),
            
           
            ('GRID', (0, 0), (-1, -1), 1, border_color),
            ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
            ('LEFTPADDING', (0, 0), (-1, -1), 10),
            ('RIGHTPADDING', (0, 0), (-1, -1), 10),
            ('TOPPADDING', (0, 1), (-1, -1), 10),
            ('BOTTOMPADDING', (0, 1), (-1, -1), 10),
        ]
        
        table.setStyle(TableStyle(style_commands))
        return table
    
    def generate(self, data: List[Any], **kwargs) -> Tuple[BytesIO, str, str]:

        try:
            self.validate_data(data)
            
            buffer = BytesIO()
            doc = SimpleDocTemplate(
                buffer,
                pagesize=A4,
                topMargin=0.75*inch,
                bottomMargin=0.75*inch,
                leftMargin=0.5*inch,
                rightMargin=0.5*inch
            )
            
            elements = []
            
      
            title_style = ParagraphStyle(
                'CustomTitle',
                parent=getSampleStyleSheet()['Heading1'],
                fontSize=18,
                textColor=HexColor('#2C3E50'),
                spaceAfter=12,
                alignment=TA_CENTER,
                fontName='Helvetica-Bold'
            )
            elements.append(Paragraph(self.title, title_style))
            
           
            date_style = ParagraphStyle(
                'DateStyle',
                parent=getSampleStyleSheet()['Normal'],
                fontSize=9,
                textColor=HexColor('#7F8C8D'),
                alignment=TA_RIGHT,
                spaceAfter=12
            )
            elements.append(Paragraph(
                f"Emitido em: {datetime.now().strftime('%d/%m/%Y às %H:%M:%S')}",
                date_style
            ))
            
  
            elements.append(Spacer(1, 0.2*inch))
      
            table_data = self._build_table_data(data)
            if table_data and len(table_data) > 1:
                table = self._create_styled_table(table_data)
                elements.append(table)
            else:
                elements.append(Paragraph(
                    "Nenhum dado disponível para o relatório.",
                    getSampleStyleSheet()['Normal']
                ))
            
           
            elements.append(Spacer(1, 0.3*inch))
            footer_style = ParagraphStyle(
                'Footer',
                parent=getSampleStyleSheet()['Normal'],
                fontSize=8,
                textColor=HexColor('#95A5A6'),
                alignment=TA_CENTER
            )
            elements.append(Paragraph(
                "Sistema FinanCerto - Relatório Gerado Automaticamente",
                footer_style
            ))
      
            doc.build(elements)
            
            buffer.seek(0)
            timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
            filename = f"relatorio_{timestamp}.pdf"
            
            self.logger.info(f"Relatório PDF gerado com sucesso: {filename}")
            return buffer, "application/pdf", filename
        
        except Exception as e:
            self.logger.error(f"Erro ao gerar PDF: {e}")
            raise

class ReportService:
  
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
        self.logger = logging.getLogger(self.__class__.__name__)
    
    def _validate_report_type(self, report_type: str) -> None:
      
        if not report_type:
            raise ValueError("Tipo de relatório não pode estar vazio")
        if report_type not in self.REPORTS:
            valid_types = ', '.join(self.REPORTS.keys())
            raise ValueError(f"Tipo de relatório inválido: '{report_type}'. Tipos válidos: {valid_types}")
        self.logger.debug(f"Tipo de relatório validado: {report_type}")
    
    def _validate_format_type(self, format_type: str) -> str:
       
        if not format_type:
            raise ValueError("Tipo de formato não pode estar vazio")
        
        normalized_format = format_type.lower().strip()
        if normalized_format not in ['excel', 'pdf']:
            raise ValueError(f"Formato inválido: '{format_type}'. Formatos suportados: excel, pdf")
        
        self.logger.debug(f"Formato de saída validado: {normalized_format}")
        return normalized_format
    
    def _get_generator(self, report_type: str, format_type: str) -> ReportGenerator:
        
        self._validate_report_type(report_type)
        normalized_format = self._validate_format_type(format_type)
        
        report_config = self.REPORTS[report_type]
        
        try:
            if normalized_format == 'excel':
                self.logger.info(f"Gerando ExcelGenerator para: {report_type}")
                return ExcelGenerator(
                    columns=report_config['columns'],
                    sheet_name=report_config['title'][:31] 
                )
            else:  
                self.logger.info(f"Gerando PDFGenerator para: {report_type}")
                return PDFGenerator(
                    title=report_config['title'],
                    columns=report_config['columns']
                )
        except Exception as e:
            self.logger.error(f"Erro ao criar gerador para {report_type}/{normalized_format}: {e}")
            raise
    
    async def gerar_relatorio(self, 
                           report_type: str,
                           format_type: str = 'excel',
                           **filters) -> Tuple[BytesIO, str, str]:
      
        try:
            start_time = datetime.now()
            self.logger.info(f"Iniciando geração de relatório: type={report_type}, format={format_type}")
   
            self._validate_report_type(report_type)
            self._validate_format_type(format_type)
            
           
            try:
                if report_type == 'saldo_mensal':
                    self.logger.debug(f"Buscando dados de saldo mensal com filtros: {filters}")
                    data = self.report_repository.buscar_saldo_mensal(
                        usuario_id=filters.get('usuario_id'),
                        ano=filters.get('ano'),
                        conta_id=filters.get('conta_id')
                    )
                elif report_type == 'transacoes_detalhadas':
                    self.logger.debug(f"Buscando transações detalhadas com filtros: {filters}")
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
                    self.logger.debug(f"Buscando transações por categoria com filtros: {filters}")
                    data = self.report_repository.buscar_relatorio(**filters)
                
            except Exception as e:
                self.logger.error(f"Erro ao buscar dados para relatório: {e}")
                raise ValueError(f"Erro ao recuperar dados do relatório: {str(e)}")
            
            if not data:
                self.logger.warning(f"Nenhum dado encontrado para {report_type} com filtros: {filters}")
                raise ValueError(f"Nenhum dado encontrado para os filtros fornecidos")
            
            self.logger.debug(f"Dados recuperados: {len(data)} registros")
            
          
            generator = self._get_generator(report_type, format_type)
            
            
            buffer, mime_type, filename = generator.generate(data)
            
        
            elapsed_time = (datetime.now() - start_time).total_seconds()
            self.logger.info(f"Relatório gerado com sucesso: {filename} ({len(data)} registros em {elapsed_time:.2f}s)")
            
            return buffer, mime_type, filename
        
        except (ValueError, TypeError) as e:
            self.logger.error(f"Erro de validação ao gerar relatório: {e}")
            raise
        except Exception as e:
            self.logger.error(f"Erro inesperado ao gerar relatório: {e}", exc_info=True)
            raise ValueError(f"Erro ao gerar relatório: {str(e)}")
\
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
        