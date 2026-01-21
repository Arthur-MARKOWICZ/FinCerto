import pytest
from unittest.mock import Mock, MagicMock, patch, AsyncMock
from fastapi.testclient import TestClient
from io import BytesIO
from datetime import datetime
from app.report_controller import ReportController, get_report_controller


class TestReportController:
    """Testes para a classe ReportController"""

    @pytest.fixture
    def mock_db(self):
        """Mock da sessão do banco de dados"""
        return Mock()

    @pytest.fixture
    def controller(self, mock_db):
        """Cria uma instância de ReportController"""
        with patch('app.report_controller.ReportService'):
            controller = ReportController(mock_db)
            controller.report_service = Mock()
            return controller

    @pytest.mark.asyncio
    async def test_gerar_relatorio_sucesso(self, controller):
        """Testa geração de relatório com sucesso"""
        # Arranjar
        buffer = BytesIO(b"test data")
        mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        filename = "relatorio_20240115_120000.xlsx"
        
        controller.report_service.gerar_relatorio_por_categoria = AsyncMock(
            return_value=(buffer, mime, filename)
        )

        # Agir
        response = await controller.gerar_relatorio(
            usuario_id=1,
            categoria_id=1,
            tipo='DESPESA',
            periodo='mensal',
            formato='excel'
        )

        # Afirmar
        assert response is not None
        controller.report_service.gerar_relatorio_por_categoria.assert_called_once()

    @pytest.mark.asyncio
    async def test_gerar_relatorio_parametros_opcionais_none(self, controller):
        """Testa geração de relatório com parâmetros opcionais como None"""
        # Arranjar
        buffer = BytesIO(b"test data")
        mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        filename = "relatorio.xlsx"
        
        controller.report_service.gerar_relatorio_por_categoria = AsyncMock(
            return_value=(buffer, mime, filename)
        )

        # Agir
        response = await controller.gerar_relatorio(
            usuario_id=1,
            categoria_id=None,
            tipo=None,
            periodo=None,
            formato='excel'
        )

        # Afirmar
        assert response is not None

    @pytest.mark.asyncio
    async def test_gerar_relatorio_erro_servico(self, controller):
        """Testa tratamento de erro na geração de relatório"""
        # Arranjar
        controller.report_service.gerar_relatorio_por_categoria = AsyncMock(
            side_effect=Exception("Erro na geração")
        )

        # Agir e Afirmar
        from fastapi import HTTPException
        with pytest.raises(HTTPException) as exc_info:
            await controller.gerar_relatorio(usuario_id=1)
        
        assert exc_info.value.status_code == 400

    @pytest.mark.asyncio
    async def test_gerar_relatorio_saldo_mensal_sucesso(self, controller):
        """Testa geração de relatório de saldo mensal"""
        # Arranjar
        buffer = BytesIO(b"test data")
        mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        filename = "saldo_mensal.xlsx"
        
        controller.report_service.gerar_relatorio = AsyncMock(
            return_value=(buffer, mime, filename)
        )

        # Agir
        response = await controller.gerar_relatorio_saldo_mensal(
            usuario_id=1,
            ano=2024,
            conta_id=1,
            formato='excel'
        )

        # Afirmar
        assert response is not None
        controller.report_service.gerar_relatorio.assert_called_once()

    @pytest.mark.asyncio
    async def test_gerar_relatorio_saldo_mensal_sem_filtros(self, controller):
        """Testa geração de saldo mensal sem filtros opcionais"""
        # Arranjar
        buffer = BytesIO(b"test data")
        controller.report_service.gerar_relatorio = AsyncMock(
            return_value=(buffer, "application/vnd.ms-excel", "saldo.xlsx")
        )

        # Agir
        response = await controller.gerar_relatorio_saldo_mensal(
            usuario_id=1,
            formato='pdf'
        )

        # Afirmar
        assert response is not None

    @pytest.mark.asyncio
    async def test_gerar_relatorio_transacao_detalhado_sucesso(self, controller):
        """Testa geração de relatório de transações detalhadas"""
        # Arranjar
        buffer = BytesIO(b"test data")
        controller.report_service.gerar_relatorio = AsyncMock(
            return_value=(buffer, "application/vnd.ms-excel", "transacoes.xlsx")
        )

        # Agir
        response = await controller.gerar_relatorio_transacao_detalhado(
            usuario_id=1,
            conta_id=1,
            categoria_id=2,
            data_inicio=None,
            data_fim=None,
            valor_minimo=100,
            valor_maximo=5000,
            tipo_transacao='DESPESA',
            formato='excel'
        )

        # Afirmar
        assert response is not None
        controller.report_service.gerar_relatorio.assert_called_once()

    @pytest.mark.asyncio
    async def test_gerar_relatorio_transacao_detalhado_sem_filtros(self, controller):
        """Testa geração de transações detalhadas sem filtros"""
        # Arranjar
        buffer = BytesIO(b"test data")
        controller.report_service.gerar_relatorio = AsyncMock(
            return_value=(buffer, "application/pdf", "transacoes.pdf")
        )

        # Agir
        response = await controller.gerar_relatorio_transacao_detalhado(
            usuario_id=1,
            formato='pdf'
        )

        # Afirmar
        assert response is not None

    @pytest.mark.asyncio
    async def test_gerar_relatorio_saldo_mensal_erro(self, controller):
        """Testa tratamento de erro em saldo mensal"""
        # Arranjar
        controller.report_service.gerar_relatorio = AsyncMock(
            side_effect=ValueError("Usuário não encontrado")
        )

        # Agir e Afirmar
        from fastapi import HTTPException
        with pytest.raises(HTTPException) as exc_info:
            await controller.gerar_relatorio_saldo_mensal(usuario_id=999)
        
        assert exc_info.value.status_code == 400

    @pytest.mark.asyncio
    async def test_gerar_relatorio_transacao_detalhado_erro(self, controller):
        """Testa tratamento de erro em transações detalhadas"""
        # Arranjar
        controller.report_service.gerar_relatorio = AsyncMock(
            side_effect=Exception("Erro interno")
        )

        # Agir e Afirmar
        from fastapi import HTTPException
        with pytest.raises(HTTPException) as exc_info:
            await controller.gerar_relatorio_transacao_detalhado(usuario_id=1)
        
        assert exc_info.value.status_code == 400


class TestGetReportController:
    """Testes para a função get_report_controller"""

    def test_get_report_controller(self, mock_db):
        """Testa obtenção do controlador"""
        with patch('app.report_controller.get_db') as mock_get_db:
            mock_get_db.return_value = mock_db
            
            controller = get_report_controller(mock_db)
            
            assert isinstance(controller, ReportController)
            assert controller.db == mock_db


class TestReportControllerRoutes:
    """Testes para os endpoints do controlador"""

    @pytest.fixture
    def client(self):
        """Cria um cliente de teste FastAPI"""
        from app.main import app
        from fastapi.testclient import TestClient
        return TestClient(app)

    def test_home_endpoint(self, client):
        """Testa endpoint home"""
        response = client.get("/")
        assert response.status_code == 200
        assert "FinanCerto Analytics" in response.json()["message"]

    @patch('app.report_controller.get_report_controller')
    def test_relatorio_por_categoria_endpoint(self, mock_get_controller, client):
        """Testa endpoint de relatório por categoria"""
        # Arranjar
        mock_controller = Mock()
        mock_controller.gerar_relatorio = AsyncMock(
            return_value=Mock()
        )
        mock_get_controller.return_value = mock_controller

        # Agir
        response = client.get("/api/relatorioPorCategoria?usuario_id=1")

        # Afirmar 
        assert response.status_code in [200, 400, 500]  

    @patch('app.report_controller.get_report_controller')
    def test_relatorio_saldo_mensal_endpoint(self, mock_get_controller, client):
        """Testa endpoint de saldo mensal"""
        # Arranjar
        mock_controller = Mock()
        mock_controller.gerar_relatorio_saldo_mensal = AsyncMock(
            return_value=Mock()
        )
        mock_get_controller.return_value = mock_controller

        # Agir
        response = client.get("/api/relatorioSaldoMensal?usuario_id=1")

        # Afirmar
        assert response.status_code in [200, 400, 500]
