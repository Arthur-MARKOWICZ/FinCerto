import React, { useState } from 'react';
import LoginForm from './components/LoginForm';
import CadastroForm from './components/CadastroForm';
import ContasDashboard from './components/ContasDashboard';
import ContaDetalhe from './components/ContaDetalhe';
import OrcamentoPage from './pages/OrcamentoPage';
import CategoriaPage from './pages/CategoriaPage';
import { Conta } from './types/conta';

type Tela = 'login' | 'cadastro' | 'dashboard' | 'contaDetalhe' | 'orcamentos' | 'categorias';

function App() {
  const [telaAtual, setTelaAtual] = useState<Tela>('login');
  const [contaSelecionada, setContaSelecionada] = useState<Conta | null>(null);

  const handleCadastroSucesso = () => {
    setTelaAtual('login');
  };

  const handleLoginSucesso = (token: string) => {
    setTelaAtual('dashboard');
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setTelaAtual('login');
    setContaSelecionada(null);
  };

  const handleContaSelect = (conta: Conta) => {
    setContaSelecionada(conta);
    setTelaAtual('contaDetalhe');
  };

  const handleVoltarParaContas = () => {
    setContaSelecionada(null);
    setTelaAtual('dashboard');
  };

  const handleContaAtualizada = () => {
    // Não força mais a mudança de tela, apenas atualiza os dados
    // A mudança de tela agora é controlada pelo usuário
  };

  const handleTrocarConta = () => {
    setTelaAtual('dashboard');
  };

  const handleIrParaOrcamentos = () => {
    setTelaAtual('orcamentos');
  };

  const handleIrParaCategorias = () => {
    setTelaAtual('categorias');
  };

  const renderTela = () => {
    switch (telaAtual) {
      case 'login':
        return (
          <LoginForm
            onSuccess={handleLoginSucesso}
            onCadastroClick={() => setTelaAtual('cadastro')}
          />
        );
      
      case 'cadastro':
        return (
          <CadastroForm
            onSuccess={handleCadastroSucesso}
            onLoginClick={() => setTelaAtual('login')}
          />
        );
      
      case 'dashboard':
        return (
          <div className="min-h-screen bg-gray-50">
            <nav className="bg-white shadow">
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                  <div className="flex items-center">
                    <h1 className="text-xl font-bold text-gray-900">FinCerto</h1>
                  </div>
                  <div className="flex items-center space-x-4">
                    <button
                      onClick={handleIrParaCategorias}
                      className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                    >
                      Categorias
                    </button>
                    <button
                      onClick={handleIrParaOrcamentos}
                      className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                    >
                      Orçamentos
                    </button>
                    <button
                      onClick={handleLogout}
                      className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                    >
                      Sair
                    </button>
                  </div>
                </div>
              </div>
            </nav>
            <ContasDashboard 
              onContaSelect={handleContaSelect} 
              contaAtualId={contaSelecionada?.id}
            />
          </div>
        );
      
      case 'contaDetalhe':
        return (
          <div className="min-h-screen bg-gray-50">
            <nav className="bg-white shadow">
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                  <div className="flex items-center">
                    <h1 className="text-xl font-bold text-gray-900">FinCerto</h1>
                  </div>
                  <div className="flex items-center">
                    <button
                      onClick={handleLogout}
                      className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                    >
                      Sair
                    </button>
                  </div>
                </div>
              </div>
            </nav>
            {contaSelecionada && (
              <ContaDetalhe
                conta={contaSelecionada}
                onVoltar={handleVoltarParaContas}
                onContaAtualizada={handleContaAtualizada}
                onTrocarConta={handleTrocarConta}
              />
            )}
          </div>
        );
      
      case 'orcamentos':
        return (
          <div className="min-h-screen bg-gray-50">
            <nav className="bg-white shadow">
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                  <div className="flex items-center">
                    <button
                      onClick={() => setTelaAtual('dashboard')}
                      className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                    >
                      ← Voltar
                    </button>
                    <h1 className="text-xl font-bold text-gray-900 ml-4">Orçamentos</h1>
                  </div>
                  <div className="flex items-center">
                    <button
                      onClick={handleLogout}
                      className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                    >
                      Sair
                    </button>
                  </div>
                </div>
              </div>
            </nav>
            <OrcamentoPage />
          </div>
        );
      
      case 'categorias':
        return (
          <div className="min-h-screen bg-gray-50">
            <nav className="bg-white shadow">
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                  <div className="flex items-center">
                    <button
                      onClick={() => setTelaAtual('dashboard')}
                      className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                    >
                      ← Voltar
                    </button>
                    <h1 className="text-xl font-bold text-gray-900 ml-4">Categorias</h1>
                  </div>
                  <div className="flex items-center">
                    <button
                      onClick={handleLogout}
                      className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                    >
                      Sair
                    </button>
                  </div>
                </div>
              </div>
            </nav>
            <CategoriaPage />
          </div>
        );
      
      default:
        return null;
    }
  };

  return (
    <div>
      {renderTela()}
    </div>
  );
}

export default App;
