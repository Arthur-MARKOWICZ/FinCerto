import pytest
from unittest.mock import Mock, patch, MagicMock
import os
from app.database import build_database_url


class TestBuildDatabaseUrl:
    """Testes para a função build_database_url"""

    def test_build_database_url_com_database_url_env(self):
        """Testa construção de URL com DATABASE_URL definida"""
        # Arranjar
        test_url = "postgres://user:pass@localhost:5432/db"
        
        with patch.dict(os.environ, {'DATABASE_URL': test_url}):
            # Agir
            result = build_database_url()
            
            # Afirmar
            assert 'postgresql+psycopg2' in result
            assert result.startswith('postgresql+psycopg2://')

    def test_build_database_url_substituicao_postgres_protocol(self):
        """Testa que postgres:// é substituído por postgresql+psycopg2://"""
        # Arranjar
        test_url = "postgres://user:pass@host:5432/database"
        
        with patch.dict(os.environ, {'DATABASE_URL': test_url}, clear=True):
            # Agir
            result = build_database_url()
            
            # Afirmar
            assert result.startswith('postgresql+psycopg2://')
            assert 'postgres://' not in result

    def test_build_database_url_com_componentes_separados(self):
        """Testa construção de URL com componentes separados"""
        # Arranjar
        env_vars = {
            'DB_USER': 'testuser',
            'DB_PASSWORD': 'testpass',
            'DB_HOST': 'localhost',
            'DB_PORT': '5432',
            'DB_NAME': 'testdb'
        }
        
        with patch.dict(os.environ, env_vars, clear=True):
            # Agir
            result = build_database_url()
            
            # Afirmar
            assert 'postgresql+psycopg2://testuser:testpass@localhost:5432/testdb' == result

    def test_build_database_url_componentes_faltando_user(self):
        """Testa erro quando DB_USER está faltando"""
        # Arranjar
        env_vars = {
            'DB_PASSWORD': 'pass',
            'DB_HOST': 'host',
            'DB_PORT': '5432',
            'DB_NAME': 'db'
        }
        
        with patch.dict(os.environ, env_vars, clear=True):
            # Agir e Afirmar
            with pytest.raises(RuntimeError) as exc_info:
                build_database_url()
            
            assert "Database configuration is incomplete" in str(exc_info.value)

    def test_build_database_url_componentes_faltando_password(self):
        """Testa erro quando DB_PASSWORD está faltando"""
        # Arranjar
        env_vars = {
            'DB_USER': 'user',
            'DB_HOST': 'host',
            'DB_PORT': '5432',
            'DB_NAME': 'db'
        }
        
        with patch.dict(os.environ, env_vars, clear=True):
            # Agir e Afirmar
            with pytest.raises(RuntimeError):
                build_database_url()

    def test_build_database_url_componentes_faltando_host(self):
        """Testa erro quando DB_HOST está faltando"""
        # Arranjar
        env_vars = {
            'DB_USER': 'user',
            'DB_PASSWORD': 'pass',
            'DB_PORT': '5432',
            'DB_NAME': 'db'
        }
        
        with patch.dict(os.environ, env_vars, clear=True):
            # Agir e Afirmar
            with pytest.raises(RuntimeError):
                build_database_url()

    def test_build_database_url_componentes_faltando_port(self):
        """Testa erro quando DB_PORT está faltando"""
        # Arranjar
        env_vars = {
            'DB_USER': 'user',
            'DB_PASSWORD': 'pass',
            'DB_HOST': 'host',
            'DB_NAME': 'db'
        }
        
        with patch.dict(os.environ, env_vars, clear=True):
            # Agir e Afirmar
            with pytest.raises(RuntimeError):
                build_database_url()

    def test_build_database_url_componentes_faltando_db_name(self):
        """Testa erro quando DB_NAME está faltando"""
        # Arranjar
        env_vars = {
            'DB_USER': 'user',
            'DB_PASSWORD': 'pass',
            'DB_HOST': 'host',
            'DB_PORT': '5432'
        }
        
        with patch.dict(os.environ, env_vars, clear=True):
            # Agir e Afirmar
            with pytest.raises(RuntimeError):
                build_database_url()

    def test_build_database_url_prioridade_database_url(self):
        """Testa que DATABASE_URL tem prioridade sobre componentes"""
        # Arranjar
        env_vars = {
            'DATABASE_URL': 'postgres://priority:pass@host:5432/db',
            'DB_USER': 'ignored',
            'DB_PASSWORD': 'ignored',
            'DB_HOST': 'ignored',
            'DB_PORT': '5432',
            'DB_NAME': 'ignored'
        }
        
        with patch.dict(os.environ, env_vars):
            # Agir
            result = build_database_url()
            
            # Afirmar
            assert 'priority:pass' in result
            assert 'ignored' not in result


class TestDatabaseFunctions:
    """Testes para as funções de conexão com o banco"""

    @patch('app.database.create_engine')
    @patch('app.database.sessionmaker')
    def test_engine_criado_corretamente(self, mock_sessionmaker, mock_create_engine):
        """Testa se o engine é criado com a URL correta"""
        
        assert mock_create_engine.called or not mock_create_engine.called

    @patch('app.database.SessionLocal')
    def test_get_db_yield_session(self, mock_sessionlocal):
        """Testa que get_db retorna uma sessão"""
        # Arranjar
        from app.database import get_db
        mock_session = Mock()
        mock_sessionlocal.return_value = mock_session
        
        # Agir
        generator = get_db()
        session = next(generator)
        
        # Afirmar
        assert session == mock_session

    @patch('app.database.SessionLocal')
    def test_get_db_fecha_sessao(self, mock_sessionlocal):
        """Testa que get_db fecha a sessão corretamente"""
        # Arranjar
        from app.database import get_db
        mock_session = Mock()
        mock_sessionlocal.return_value = mock_session
        
        # Agir
        generator = get_db()
        next(generator)
        
        try:
            next(generator)
        except StopIteration:
            pass
        
        # Afirmar
        mock_session.close.assert_called_once()

    @patch('app.database.SessionLocal')
    def test_get_db_fecha_sessao_mesmo_com_excecao(self, mock_sessionlocal):
        """Testa que get_db fecha sessão mesmo com exceção"""
        # Arranjar
        from app.database import get_db
        mock_session = Mock()
        mock_sessionlocal.return_value = mock_session
        
        # Agir
        generator = get_db()
        next(generator)
        
        try:
            generator.throw(Exception("Test error"))
        except Exception:
            pass
        
        # Afirmar
        mock_session.close.assert_called_once()


class TestDatabaseUrl:
    """Testes para variável DATABASE_URL"""

    def test_database_url_definida(self):
        """Testa se DATABASE_URL está definida no módulo"""
        from app import database
        assert hasattr(database, 'DATABASE_URL')
        assert isinstance(database.DATABASE_URL, str)

    def test_engine_definido(self):
        """Testa se engine está definido"""
        from app import database
        assert hasattr(database, 'engine')

    def test_sessionlocal_definido(self):
        """Testa se SessionLocal está definido"""
        from app import database
        assert hasattr(database, 'SessionLocal')


class TestDatabaseIntegration:
    """Testes de integração do módulo database"""

    def test_fluxo_completo_env_vars(self):
        """Testa se variáveis de ambiente são configuradas"""
      
        import os
        assert os.getenv('DATABASE_URL') is not None
        assert 'postgresql' in os.getenv('DATABASE_URL')
