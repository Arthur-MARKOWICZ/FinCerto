import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import LoginForm from './components/LoginForm';
import CadastroForm from './components/CadastroForm';
import ContasDashboard from './components/ContasDashboard';
import ContaDetalhe from './components/ContaDetalhe';
import OrcamentoPage from './pages/OrcamentoPage';
import CategoriaPage from './pages/CategoriaPage';
import RelatorioPage from './pages/RelatorioPage';
import { Conta } from './types/conta';
import { AuthProvider, useAuth } from './hooks/useAuth';

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
}

function MainApp() {
  const { isAuthenticated, logout } = useAuth();
  const [contaSelecionada, setContaSelecionada] = useState<Conta | null>(null);
  const navigate = useNavigate();

  const handleContaSelect = (conta: Conta) => {
    setContaSelecionada(conta);
    navigate('/conta');
  };

  const handleVoltar = () => {
    setContaSelecionada(null);
    navigate('/');
  };

  if (!isAuthenticated) {
    return (
      <Routes>
        <Route path="/login" element={<LoginForm />} />
        <Route path="/cadastro" element={<CadastroForm />} />
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    );
  }

  const Layout = ({ children }: { children: React.ReactNode }) => (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <a href="/" className="text-xl font-bold text-gray-900">FinCerto</a>
            </div>
            <div className="flex items-center space-x-4">
              <a href="/categorias" className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                Categorias
              </a>
              <a href="/orcamentos" className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                Orçamentos
              </a>
              <a href="/relatorios" className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                Relatórios
              </a>
              <button onClick={logout} className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                Sair
              </button>
            </div>
          </div>
        </div>
      </nav>
      {children}
    </div>
  );

  return (
    <Layout>
      <Routes>
        <Route path="/" element={<ContasDashboard onContaSelect={handleContaSelect} contaAtualId={contaSelecionada?.id} />} />
        <Route path="/conta" element={contaSelecionada ? <ContaDetalhe conta={contaSelecionada} onVoltar={handleVoltar} onContaAtualizada={() => {}} onTrocarConta={handleVoltar} /> : <Navigate to="/" />} />
        <Route path="/orcamentos" element={<OrcamentoPage />} />
        <Route path="/categorias" element={<CategoriaPage />} />
        <Route path="/relatorios" element={<RelatorioPage />} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Layout>
  );
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <MainApp />
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
