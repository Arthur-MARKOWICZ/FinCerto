import pytest
from unittest.mock import Mock, MagicMock, patch
from sqlalchemy.orm import Session
from datetime import datetime, date
from app.report_repository import ReportRepository


class TestReportRepository:
    """Testes para a classe ReportRepository"""

    @pytest.fixture
    def repository(self, mock_db):
        """Cria uma instância de ReportRepository com mock do banco"""
        return ReportRepository(mock_db)

    def test_init(self, mock_db):
        """Testa inicialização do repositório"""
        repo = ReportRepository(mock_db)
        assert repo.session == mock_db

    def test_buscar_relatorio_com_todos_parametros(self, repository, mock_db):
        """Testa busca de relatório com todos os parâmetros"""
        # Arranjar
        mock_result = Mock()
        mock_result.fetchall.return_value = [
            Mock(data=datetime(2024, 1, 15), descricao='Teste', categoria='Cat1', 
                 conta='Conta1', valor=100.0, tipo='DESPESA'),
        ]
        mock_db.execute.return_value = mock_result

        # Agir
        result = repository.buscar_relatorio(
            usuario_id=1,
            categoria_id=1,
            tipo='DESPESA',
            periodo='mensal',
            conta_id=1,
            data_inicio=date(2024, 1, 1),
            data_fim=date(2024, 1, 31),
            valor_minimo=50.0,
            valor_maximo=200.0
        )

        # Afirmar
        assert len(result) == 1
        mock_db.execute.assert_called_once()

    def test_buscar_relatorio_apenas_usuario_id(self, repository, mock_db):
        """Testa busca de relatório com apenas usuario_id"""
        # Arranjar
        mock_result = Mock()
        mock_result.fetchall.return_value = []
        mock_db.execute.return_value = mock_result

        # Agir
        result = repository.buscar_relatorio(usuario_id=1)

        # Afirmar
        assert isinstance(result, list)
        mock_db.execute.assert_called_once()

    def test_buscar_transacoes_detalhadas_completo(self, repository, mock_db, sample_transaction_data):
        """Testa busca de transações detalhadas com sucesso"""
        # Arranjar
        mock_result = Mock()
        mock_rows = [
            (1, datetime(2024, 1, 15), 'Supermercado', 150.50, 'DESPESA', 
             'Alimentação', 'Conta Corrente', 'CORRENTE'),
            (2, datetime(2024, 1, 20), 'Salário', 3000.00, 'RECEITA', 
             'Renda', 'Conta Corrente', 'CORRENTE'),
        ]
        mock_result.fetchall.return_value = mock_rows
        mock_db.execute.return_value = mock_result

        # Agir
        result = repository.buscar_transacoes_detalhadas(usuario_id=1)

        # Afirmar
        assert len(result) == 2
        assert result[0]['id'] == 1
        assert result[0]['descricao'] == 'Supermercado'
        assert result[0]['valor'] == 150.50
        assert result[0]['tipo_transacao'] == 'DESPESA'

    def test_buscar_transacoes_com_filtros(self, repository, mock_db):
        """Testa busca de transações com múltiplos filtros"""
        # Arranjar
        mock_result = Mock()
        mock_result.fetchall.return_value = []
        mock_db.execute.return_value = mock_result

        # Agir
        result = repository.buscar_transacoes_detalhadas(
            usuario_id=1,
            conta_id=2,
            categoria_id=3,
            data_inicio=date(2024, 1, 1),
            data_fim=date(2024, 1, 31),
            valor_minimo=100.0,
            valor_maximo=500.0,
            tipo_transacao='DESPESA'
        )

        # Afirmar
        assert isinstance(result, list)
        mock_db.execute.assert_called_once()

    def test_buscar_transacoes_sem_categoria_nem_conta(self, repository, mock_db):
        """Testa conversão quando categoria e conta são None"""
        # Arranjar
        mock_result = Mock()
        mock_rows = [
            (1, datetime(2024, 1, 15), 'Transação', 100.0, 'DESPESA', 
             None, None, None),
        ]
        mock_result.fetchall.return_value = mock_rows
        mock_db.execute.return_value = mock_result

        # Agir
        result = repository.buscar_transacoes_detalhadas(usuario_id=1)

        # Afirmar
        assert result[0]['categoria'] == 'Sem Categoria'
        assert result[0]['conta'] == 'Sem Conta'
        assert result[0]['tipo_conta'] == 'OUTRO'

    def test_buscar_saldo_mensal_com_usuario_valido(self, repository, mock_db):
        """Testa busca de saldo mensal com usuário válido"""
        # Arranjar
        mock_result = Mock()
        mock_result.fetchone.return_value = (1,)
        mock_result.scalar.return_value = 10  # Total transações
        mock_result.fetchall.return_value = []  # Sem dados de saldo
        
        mock_db.execute.return_value = mock_result
        
        # Agir
        result = repository.buscar_saldo_mensal(usuario_id=1, ano=2024)
        
        # Afirmar
        assert isinstance(result, list)
        assert len(mock_db.execute.call_args_list) >= 1

    def test_buscar_saldo_mensal_usuario_nao_existe(self, repository, mock_db):
        """Testa busca de saldo mensal quando usuário não existe"""
        # Arranjar
        user_check = Mock()
        user_check.fetchone.return_value = None
        mock_db.execute.return_value = user_check

        # Agir
        result = repository.buscar_saldo_mensal(usuario_id=999)

        # Afirmar
        assert result == []

    def test_buscar_saldo_mensal_com_filtro_ano(self, repository, mock_db):
        """Testa busca de saldo mensal com filtro de ano"""
        # Arranjar
        mock_result = Mock()
        mock_result.fetchone.return_value = (1,)
        mock_result.fetchall.return_value = []
        
        mock_db.execute.return_value = mock_result

        # Agir
        result = repository.buscar_saldo_mensal(usuario_id=1, ano=2023)

        # Afirmar
        assert isinstance(result, list)

    def test_buscar_saldo_mensal_com_filtro_conta(self, repository, mock_db):
        """Testa busca de saldo mensal com filtro de conta"""
        # Arranjar
        mock_result = Mock()
        mock_result.fetchone.return_value = (1,)
        mock_result.scalar.return_value = 5
        mock_result.fetchall.return_value = []
        
        mock_db.execute.return_value = mock_result
        
        # Agir
        result = repository.buscar_saldo_mensal(usuario_id=1, ano=2024, conta_id=1)
        
        # Afirmar
        assert isinstance(result, list)

    def test_buscar_relatorio_valores_nulos_convertidos(self, repository, mock_db):
        """Testa que valores nulos são convertidos para 0.0"""
        # Arranjar
        mock_result = Mock()
        mock_rows = [
            (1, datetime(2024, 1, 15), 'Teste', None, 'DESPESA', 
             'Cat', 'Conta', 'TIPO'),
        ]
        mock_result.fetchall.return_value = mock_rows
        mock_db.execute.return_value = mock_result

        # Agir
        result = repository.buscar_transacoes_detalhadas(usuario_id=1)

        # Afirmar
        assert result[0]['valor'] == 0.0
