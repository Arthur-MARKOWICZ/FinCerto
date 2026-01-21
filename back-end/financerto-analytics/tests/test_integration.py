import pytest
from unittest.mock import Mock, patch, MagicMock, AsyncMock
from datetime import datetime, date
from io import BytesIO


class TestReportServiceIntegration:
    """Testes de integração do ReportService com Repository"""

    @pytest.mark.asyncio
    async def test_fluxo_completo_relatorio_excel(self):
        """Testa o fluxo completo de geração de relatório Excel"""
        # Arranjar
        from app.report_repository import ReportRepository
        from app.report_service import ReportService
        
        mock_db = Mock()
        repo = ReportRepository(mock_db)
        service = ReportService(repo)
        
   
        mock_result = Mock()
        mock_result.fetchall.return_value = [
            Mock(data=datetime(2024, 1, 15), descricao='Teste 1', categoria='Cat1', 
                 conta='Conta1', valor=100.0, tipo='DESPESA'),
            Mock(data=datetime(2024, 1, 20), descricao='Teste 2', categoria='Cat2', 
                 conta='Conta2', valor=200.0, tipo='RECEITA'),
        ]
        mock_db.execute.return_value = mock_result
        
        # Agir
        buffer, mime, filename = await service.gerar_relatorio_por_categoria(
            usuario_id=1,
            formato='excel'
        )
        
        # Afirmar
        assert isinstance(buffer, BytesIO)
        assert 'spreadsheet' in mime
        assert filename.endswith('.xlsx')

    @pytest.mark.asyncio
    async def test_fluxo_completo_relatorio_pdf(self):
        """Testa o fluxo completo de geração de relatório PDF"""
        # Arranjar
        from app.report_repository import ReportRepository
        from app.report_service import ReportService
        
        mock_db = Mock()
        repo = ReportRepository(mock_db)
        service = ReportService(repo)
        
      
        mock_result = Mock()
        mock_result.fetchall.return_value = [
            Mock(data=datetime(2024, 1, 15), descricao='Teste', categoria='Cat', 
                 conta='Conta', valor=150.0, tipo='DESPESA'),
        ]
        mock_db.execute.return_value = mock_result
        
        # Agir
        buffer, mime, filename = await service.gerar_relatorio_por_categoria(
            usuario_id=1,
            formato='pdf'
        )
        
        # Afirmar
        assert isinstance(buffer, BytesIO)
        assert 'pdf' in mime.lower()
        assert filename.endswith('.pdf')

    @pytest.mark.asyncio
    async def test_fluxo_relatorio_saldo_mensal(self):
        """Testa fluxo de relatório de saldo mensal"""
        # Arranjar
        from app.report_repository import ReportRepository
        from app.report_service import ReportService
        
        mock_db = Mock()
        repo = ReportRepository(mock_db)
        service = ReportService(repo)
        
        balance_data = [
            {
                'mes': '2024-01',
                'mes_nome': 'January/2024',
                'receitas': 5000.00,
                'despesas': 2000.00,
                'saldo': 3000.00
            },
            {
                'mes': '2024-02',
                'mes_nome': 'February/2024',
                'receitas': 5000.00,
                'despesas': 2500.00,
                'saldo': 2500.00
            },
        ]
        
     
        repo.buscar_saldo_mensal = Mock(return_value=balance_data)
        
        # Agir
        buffer, mime, filename = await service.gerar_relatorio(
            report_type='saldo_mensal',
            format_type='excel',
            usuario_id=1,
            ano=2024
        )
        
        # Afirmar
        assert isinstance(buffer, BytesIO)
        assert filename.endswith('.xlsx')
        repo.buscar_saldo_mensal.assert_called_once()

    @pytest.mark.asyncio
    async def test_fluxo_relatorio_transacoes_detalhadas(self):
        """Testa fluxo de relatório de transações detalhadas"""
        # Arranjar
        from app.report_repository import ReportRepository
        from app.report_service import ReportService
        
        mock_db = Mock()
        repo = ReportRepository(mock_db)
        service = ReportService(repo)
        
        mock_result = Mock()
        mock_result.fetchall.return_value = [
            (1, datetime(2024, 1, 15), 'Compra', 150.50, 'DESPESA', 
             'Alimentação', 'Conta Corrente', 'CORRENTE'),
        ]
        mock_db.execute.return_value = mock_result
        
        # Agir
        buffer, mime, filename = await service.gerar_relatorio(
            report_type='transacoes_detalhadas',
            format_type='excel',
            usuario_id=1
        )
        
        # Afirmar
        assert isinstance(buffer, BytesIO)
        assert filename.endswith('.xlsx')


class TestReportControllerServiceIntegration:
    """Testes de integração do Controller com Service"""

    @pytest.mark.asyncio
    async def test_controller_chamando_service(self):
        """Testa que o controller chama o service corretamente"""
        # Arranjar
        from app.report_controller import ReportController
        
        mock_db = Mock()
        mock_service = Mock()
        mock_service.gerar_relatorio_por_categoria = AsyncMock(
            return_value=(BytesIO(b"data"), "application/vnd.ms-excel", "test.xlsx")
        )
        
        controller = ReportController(mock_db)
        controller.report_service = mock_service
        
        # Agir
        result = await controller.gerar_relatorio(usuario_id=1)
        
        # Afirmar
        mock_service.gerar_relatorio_por_categoria.assert_called_once()

    @pytest.mark.asyncio
    async def test_controller_parametros_passados_corretamente(self):
        """Testa que o controller passa parâmetros corretamente para o service"""
        # Arranjar
        from app.report_controller import ReportController
        
        mock_db = Mock()
        mock_service = Mock()
        mock_service.gerar_relatorio_por_categoria = AsyncMock(
            return_value=(BytesIO(b"data"), "application/vnd.ms-excel", "test.xlsx")
        )
        
        controller = ReportController(mock_db)
        controller.report_service = mock_service
        
        # Agir
        await controller.gerar_relatorio(
            usuario_id=1,
            categoria_id=2,
            tipo='DESPESA',
            periodo='mensal',
            formato='pdf'
        )
        
        # Afirmar
        call_args = mock_service.gerar_relatorio_por_categoria.call_args
        assert call_args[1]['usuario_id'] == 1
        assert call_args[1]['categoria_id'] == 2
        assert call_args[1]['tipo'] == 'DESPESA'
        assert call_args[1]['periodo'] == 'mensal'
        assert call_args[1]['formato'] == 'pdf'


class TestEndToEndReportGeneration:
    """Testes end-to-end de geração de relatórios"""

    @pytest.mark.asyncio
    async def test_e2e_excel_report_generation(self):
        """Testa a geração completa de um relatório Excel"""
        # Arranjar
        from app.report_service import ExcelGenerator
        
        sample_data = [
            {'data': '2024-01-15', 'descricao': 'Teste', 'valor': 100.0},
            {'data': '2024-01-20', 'descricao': 'Teste 2', 'valor': 200.0},
        ]
        
        generator = ExcelGenerator(
            columns=[
                {'field': 'data', 'label': 'Data'},
                {'field': 'descricao', 'label': 'Descrição'},
                {'field': 'valor', 'label': 'Valor'},
            ]
        )
        
        # Agir
        buffer, mime, filename = generator.generate(sample_data)
        
        # Afirmar
        assert buffer.tell() == 0  
        assert len(buffer.getvalue()) > 0 
        assert 'spreadsheet' in mime
        assert filename.endswith('.xlsx')

    def test_e2e_pdf_report_structure(self):
        """Testa a estrutura de um relatório PDF gerado"""
        # Arranjar
        from app.report_service import PDFGenerator
        
        sample_data = [
            Mock(
                data='2024-01-15',
                descricao='Teste',
                valor=100.0,
                categoria='Cat',
                conta='Conta',
                tipo='DESPESA'
            )
        ]
        
        generator = PDFGenerator(
            title='Relatório Teste',
            columns=[
                {'field': 'descricao', 'label': 'Descrição'},
                {'field': 'valor', 'label': 'Valor'},
            ]
        )
        
        # Agir
        buffer, mime, filename = generator.generate(sample_data)
        
        # Afirmar
        assert isinstance(buffer, BytesIO)
        assert 'pdf' in mime.lower()
        assert filename.endswith('.pdf')


class TestDataFlowIntegration:
    """Testes do fluxo de dados ponta a ponta"""

    @pytest.mark.asyncio
    async def test_fluxo_dados_usuario_ate_arquivo(self):
        """Testa o fluxo de dados desde o usuário até o arquivo gerado"""
        # Arranjar
        from app.report_repository import ReportRepository
        from app.report_service import ReportService, ExcelGenerator
        
        usuario_id = 1
        
  
        mock_db = Mock()
        mock_result = Mock()
        mock_result.fetchall.return_value = [
            Mock(data=datetime(2024, 1, 15), descricao='Compra', categoria='Food', 
                 conta='Main', valor=100.0, tipo='DESPESA'),
        ]
        mock_db.execute.return_value = mock_result
        
        repo = ReportRepository(mock_db)
        service = ReportService(repo)
        
        # Agir
        buffer, mime, filename = await service.gerar_relatorio_por_categoria(
            usuario_id=usuario_id,
            formato='excel'
        )
        
        # Afirmar
        assert buffer is not None
        assert len(buffer.getvalue()) > 0
        assert usuario_id > 0  

    @pytest.mark.asyncio
    async def test_fluxo_multiplos_filtros(self):
        """Testa fluxo com múltiplos filtros aplicados"""
        # Arranjar
        from app.report_repository import ReportRepository
        from app.report_service import ReportService
        
        mock_db = Mock()
        mock_result = Mock()
        mock_result.fetchall.return_value = []
        mock_db.execute.return_value = mock_result
        
        repo = ReportRepository(mock_db)
        service = ReportService(repo)
        
        # Agir
        with pytest.raises(ValueError):
            await service.gerar_relatorio_por_categoria(
                usuario_id=1,
                categoria_id=5,
                tipo='DESPESA',
                periodo='mensal',
                formato='excel'
            )
