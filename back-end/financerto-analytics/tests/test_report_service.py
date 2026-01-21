import pytest
from unittest.mock import Mock, MagicMock, patch, AsyncMock
from io import BytesIO
import pandas as pd
from datetime import datetime
from reportlab.platypus import Table
from app.report_service import ExcelGenerator, PDFGenerator, ReportService


class TestExcelGenerator:
    """Testes para a classe ExcelGenerator"""

    @pytest.fixture
    def excel_generator(self):
        """Cria uma instância de ExcelGenerator"""
        columns = [
            {'field': 'data', 'label': 'Data'},
            {'field': 'descricao', 'label': 'Descrição'},
            {'field': 'valor', 'label': 'Valor'},
        ]
        return ExcelGenerator(columns=columns, sheet_name='Transações')

    def test_init(self, excel_generator):
        """Testa inicialização do gerador Excel"""
        assert excel_generator.sheet_name == 'Transações'
        assert len(excel_generator.columns) == 3

    def test_row_to_dict_com_dicionario(self, excel_generator):
        """Testa conversão de linha para dicionário quando é dict"""
        data = {'data': '2024-01-15', 'valor': 100.0}
        result = excel_generator._row_to_dict(data)
        assert result == data

    def test_row_to_dict_com_objeto_nao_dict(self, excel_generator):
        """Testa conversão de linha para dicionário quando não é dict"""
       
        mock_obj = Mock()
        mock_obj.__dict__ = {'field': 'value'}
        
        formatted = excel_generator._format_data([mock_obj])
        assert isinstance(formatted, list)

    def test_format_data(self, excel_generator, sample_transaction_data):
        """Testa formatação de dados"""
        formatted = excel_generator._format_data(sample_transaction_data)
        assert len(formatted) == 3
        assert all(isinstance(item, dict) for item in formatted)

    def test_generate_com_dados_validos(self, excel_generator, sample_transaction_data):
        """Testa geração de Excel com dados válidos"""
        buffer, mime, filename = excel_generator.generate(sample_transaction_data)
        
        assert isinstance(buffer, BytesIO)
        assert mime == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        assert filename.startswith('relatorio_')
        assert filename.endswith('.xlsx')

    def test_generate_com_dados_vazios(self, excel_generator):
        """Testa geração de Excel com dados vazios"""
        with pytest.raises(ValueError, match="Nenhum dado fornecido"):
            excel_generator.generate([])

    def test_generate_com_none(self, excel_generator):
        """Testa geração de Excel com None"""
        with pytest.raises((ValueError, TypeError)):
            excel_generator.generate(None)

    def test_filename_formato_correto(self, excel_generator, sample_transaction_data):
        """Testa se o nome do arquivo segue o padrão esperado"""
        _, _, filename = excel_generator.generate(sample_transaction_data)
        
        assert 'relatorio_' in filename
        assert filename.endswith('.xlsx')
     
        date_part = filename.replace('relatorio_', '').replace('.xlsx', '')
        assert '_' in date_part


class TestPDFGenerator:
    """Testes para a classe PDFGenerator com formatação avançada"""

    @pytest.fixture
    def pdf_generator(self):
        """Cria uma instância de PDFGenerator com a nova assinatura"""
        columns = [
            {'field': 'data', 'header': 'Data', 'format': 'date'},
            {'field': 'descricao', 'header': 'Descrição'},
            {'field': 'valor', 'header': 'Valor', 'format': 'currency'},
        ]
        return PDFGenerator(
            title='Relatório de Transações',
            columns=columns
        )

    def test_init(self, pdf_generator):
        """Testa inicialização do gerador PDF"""
        assert pdf_generator.title == 'Relatório de Transações'
        assert pdf_generator.columns == [
            {'field': 'data', 'header': 'Data', 'format': 'date'},
            {'field': 'descricao', 'header': 'Descrição'},
            {'field': 'valor', 'header': 'Valor', 'format': 'currency'},
        ]
        assert pdf_generator.logger is not None

    def test_init_valores_padrao(self):
        """Testa inicialização com valores padrão"""
        columns = [{'field': 'data', 'header': 'Data'}]
        pdf_gen = PDFGenerator(title='Teste', columns=columns)
        assert pdf_gen.title == 'Teste'
        assert pdf_gen.columns == columns

    def test_build_table_data_com_dados_validos(self, pdf_generator, sample_transaction_data):
        """Testa construção de dados para tabela"""
        table_data = pdf_generator._build_table_data(sample_transaction_data)
        
        assert isinstance(table_data, list)
        assert len(table_data) > 0
 
        assert table_data[0] == [col.get('header', col['field']) for col in pdf_generator.columns]

    def test_build_table_data_vazio(self, pdf_generator):
        """Testa construção com dados vazios"""
        table_data = pdf_generator._build_table_data([])
        
        assert isinstance(table_data, list)

    def test_create_styled_table(self, pdf_generator, sample_transaction_data):
        """Testa criação de tabela estilizada"""
        table_data = pdf_generator._build_table_data(sample_transaction_data)
        table = pdf_generator._create_styled_table(table_data)
        
        assert table is not None
        assert isinstance(table, Table)

    def test_generate_com_dados_validos(self, pdf_generator, sample_transaction_data):
        """Testa geração de PDF com dados válidos"""
        buffer, mime, filename = pdf_generator.generate(sample_transaction_data)

        assert isinstance(buffer, BytesIO)
        assert 'pdf' in mime.lower()
        assert filename.endswith('.pdf')
        assert buffer.getbuffer().nbytes > 0

    def test_generate_com_dados_vazios(self, pdf_generator):
        """Testa geração de PDF com dados vazios"""
        with pytest.raises(ValueError, match="Nenhum dado fornecido"):
            pdf_generator.generate([])


class TestReportService:
    """Testes para a classe ReportService"""

    @pytest.fixture
    def mock_repository(self):
        """Cria um mock do repositório"""
        return Mock()

    @pytest.fixture
    def report_service(self, mock_repository):
        """Cria uma instância de ReportService"""
        return ReportService(mock_repository)

    def test_init(self, report_service, mock_repository):
        """Testa inicialização do serviço"""
        assert report_service.report_repository == mock_repository

    @pytest.mark.asyncio
    async def test_gerar_relatorio_por_categoria_excel(self, report_service, mock_repository, sample_transaction_data):
        """Testa geração de relatório por categoria em Excel"""
        # Arranjar
        mock_repository.buscar_relatorio.return_value = sample_transaction_data

        # Agir
        buffer, mime, filename = await report_service.gerar_relatorio(
            report_type='transacoes_por_categoria',
            format_type='excel',
            usuario_id=1
        )

        # Afirmar
        assert isinstance(buffer, BytesIO)
        assert 'spreadsheet' in mime
        assert filename.endswith('.xlsx')
        mock_repository.buscar_relatorio.assert_called()

    @pytest.mark.asyncio
    async def test_gerar_relatorio_por_categoria_pdf(self, report_service, mock_repository, sample_transaction_data):
        """Testa geração de relatório por categoria em PDF"""
        # Arranjar
        mock_repository.buscar_relatorio.return_value = sample_transaction_data

        # Agir
        buffer, mime, filename = await report_service.gerar_relatorio(
            report_type='transacoes_por_categoria',
            format_type='pdf',
            usuario_id=1,
            categoria_id=1
        )

        # Afirmar
        assert isinstance(buffer, BytesIO)
        assert 'pdf' in mime.lower()
        assert filename.endswith('.pdf')

    @pytest.mark.asyncio
    async def test_gerar_relatorio_por_categoria_com_filtros(self, report_service, mock_repository, sample_transaction_data):
        """Testa geração de relatório com múltiplos filtros"""
        # Arranjar
        mock_repository.buscar_relatorio.return_value = sample_transaction_data

        # Agir
        buffer, mime, filename = await report_service.gerar_relatorio(
            report_type='transacoes_por_categoria',
            format_type='excel',
            usuario_id=1,
            categoria_id=2,
            tipo='DESPESA',
            periodo='mensal'
        )

        # Afirmar
        assert isinstance(buffer, BytesIO)
        mock_repository.buscar_relatorio.assert_called()

    @pytest.mark.asyncio
    async def test_gerar_relatorio_nenhum_dado(self, report_service, mock_repository):
        """Testa geração de relatório quando não há dados"""
        # Arranjar
        mock_repository.buscar_relatorio.return_value = []

        # Agir e Afirmar
        with pytest.raises(ValueError):
            await report_service.gerar_relatorio(
                report_type='transacoes_por_categoria',
                usuario_id=1
            )

    @pytest.mark.asyncio
    async def test_gerar_relatorio_formato_invalido(self, report_service, mock_repository, sample_transaction_data):
        """Testa geração de relatório com formato inválido"""
        # Arranjar
        mock_repository.buscar_relatorio.return_value = sample_transaction_data

        # Agir e Afirmar
        with pytest.raises(ValueError):
            await report_service.gerar_relatorio(
                report_type='transacoes_por_categoria',
                format_type='xml',
                usuario_id=1
            )

    @pytest.mark.asyncio
    async def test_gerar_relatorio_saldo_mensal(self, report_service, mock_repository, sample_monthly_balance_data):
        """Testa geração de relatório de saldo mensal"""
        # Arranjar
        mock_repository.buscar_saldo_mensal.return_value = sample_monthly_balance_data

        # Agir
        buffer, mime, filename = await report_service.gerar_relatorio(
            report_type='saldo_mensal',
            format_type='excel',
            usuario_id=1,
            ano=2024
        )

        # Afirmar
        assert isinstance(buffer, BytesIO)
        mock_repository.buscar_saldo_mensal.assert_called()

    @pytest.mark.asyncio
    async def test_gerar_relatorio_transacoes_detalhadas(self, report_service, mock_repository, sample_transaction_data):
        """Testa geração de relatório de transações detalhadas"""
        # Arranjar
        mock_repository.buscar_transacoes_detalhadas.return_value = sample_transaction_data

        # Agir
        buffer, mime, filename = await report_service.gerar_relatorio(
            report_type='transacoes_detalhadas',
            format_type='excel',
            usuario_id=1
        )

        # Afirmar
        assert isinstance(buffer, BytesIO)
        mock_repository.buscar_transacoes_detalhadas.assert_called()

    @pytest.mark.asyncio
    async def test_gerar_relatorio_tipo_invalido(self, report_service, mock_repository):
        """Testa geração de relatório com tipo inválido"""
        # Arranjar e Agir e Afirmar
        with pytest.raises(ValueError):
            await report_service.gerar_relatorio(
                report_type='tipo_invalido',
                format_type='excel',
                usuario_id=1
            )
