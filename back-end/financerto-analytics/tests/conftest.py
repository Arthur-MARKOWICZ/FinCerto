import pytest
from unittest.mock import Mock, MagicMock
from sqlalchemy.orm import Session
from io import BytesIO
import pandas as pd
import os
import sys


os.environ['DATABASE_URL'] = 'postgresql+psycopg2://user:pass@localhost:5432/testdb'
os.environ['DB_USER'] = 'testuser'
os.environ['DB_PASSWORD'] = 'testpass'
os.environ['DB_HOST'] = 'localhost'
os.environ['DB_PORT'] = '5432'
os.environ['DB_NAME'] = 'testdb'


@pytest.fixture
def mock_db():
    """Fixture para mockar a sessão do banco de dados"""
    return Mock(spec=Session)


@pytest.fixture
def sample_transaction_data():
    """Dados de exemplo para transações"""
    return [
        {
            'id': 1,
            'data': '2024-01-15',
            'data_formatada': '15/01/2024',
            'descricao': 'Supermercado',
            'valor': 150.50,
            'tipo_transacao': 'DESPESA',
            'categoria': 'Alimentação',
            'conta': 'Conta Corrente',
            'tipo_conta': 'CORRENTE'
        },
        {
            'id': 2,
            'data': '2024-01-20',
            'data_formatada': '20/01/2024',
            'descricao': 'Salário',
            'valor': 3000.00,
            'tipo_transacao': 'RECEITA',
            'categoria': 'Renda',
            'conta': 'Conta Corrente',
            'tipo_conta': 'CORRENTE'
        },
        {
            'id': 3,
            'data': '2024-01-25',
            'data_formatada': '25/01/2024',
            'descricao': 'Conta de Luz',
            'valor': 180.00,
            'tipo_transacao': 'DESPESA',
            'categoria': 'Utilidades',
            'conta': 'Conta Poupança',
            'tipo_conta': 'POUPANCA'
        }
    ]


@pytest.fixture
def sample_monthly_balance_data():
    """Dados de exemplo para saldo mensal"""
    return [
        {
            'mes': 1,
            'ano': 2024,
            'mes_ano': 'Janeiro/2024',
            'receitas': 5000.00,
            'despesas': 2000.00,
            'saldo': 3000.00
        },
        {
            'mes': 2,
            'ano': 2024,
            'mes_ano': 'Fevereiro/2024',
            'receitas': 5000.00,
            'despesas': 2500.00,
            'saldo': 2500.00
        }
    ]


@pytest.fixture
def sample_category_data():
    """Dados de exemplo para categorias"""
    return [
        {
            'categoria': 'Alimentação',
            'total_despesas': 500.00,
            'percentual': 45.5
        },
        {
            'categoria': 'Utilidades',
            'total_despesas': 300.00,
            'percentual': 27.3
        },
        {
            'categoria': 'Transporte',
            'total_despesas': 300.00,
            'percentual': 27.2
        }
    ]
