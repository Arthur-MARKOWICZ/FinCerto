import React, { useState } from 'react';
import LoginForm from './components/LoginForm';
import CadastroForm from './components/CadastroForm';
import ContasDashboard from './components/ContasDashboard';
import ContaDetalhe from './components/ContaDetalhe';
import { Conta } from './types/conta';

type Tela = 'login' | 'cadastro' | 'dashboard' | 'contaDetalhe';

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
            <ContasDashboard onContaSelect={handleContaSelect} />
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
              />
            )}
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
