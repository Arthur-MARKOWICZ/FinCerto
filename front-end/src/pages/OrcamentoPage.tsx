import React, { useState, useEffect } from 'react';
import { Orcamento, OrcamentoCadastroDto } from '../types/orcamento';
import { orcamentoApiService } from '../services/orcamentoApi';
import AdicionarOrcamentoModal from '../components/AdicionarOrcamentoModal';

const OrcamentoPage: React.FC = () => {
  const [orcamentos, setOrcamentos] = useState<Orcamento[]>([]);
  const [carregando, setCarregando] = useState<boolean>(true);
  const [erro, setErro] = useState<string>('');
  const [modalAberto, setModalAberto] = useState<boolean>(false);

  const carregarOrcamentos = async () => {
    try {
      setCarregando(true);
      // Usando a função real de obter orçamentos por usuário com paginação
      const response = await orcamentoApiService.obterPorUsuarioPaginado(0, 5);
      setOrcamentos(response.content || []);
    } catch (error: any) {
      console.error('Erro ao carregar orçamentos:', error);
      setErro('Erro ao carregar orçamentos. Tente novamente mais tarde.');
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    carregarOrcamentos();
  }, []);

  const formatarValor = (valor: number): string => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor);
  };

  const formatarData = (dataString: string): string => {
    if (!dataString) return 'Não definido';
    const data = new Date(dataString);
    return data.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  const calcularPercentual = (atual: number, limite: number): number => {
    if (limite === 0) return 0;
    return (atual / limite) * 100;
  };

  const getCorProgresso = (percentual: number): string => {
    if (percentual < 50) return 'bg-green-500';
    if (percentual < 80) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  const handleOrcamentoAdicionado = () => {
    carregarOrcamentos();
  };

  if (carregando) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-6xl mx-auto px-4">
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-3xl font-bold text-gray-800">Gerenciar Orçamentos</h1>
            <button
              onClick={() => setModalAberto(true)}
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md"
            >
              Novo Orçamento
            </button>
          </div>

          {erro && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {erro}
            </div>
          )}

          {orcamentos.length === 0 ? (
            <div className="text-center py-12">
              <div className="text-gray-400 mb-4">
                <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">Nenhum orçamento encontrado</h3>
              <p className="text-gray-500 mb-4">Crie seu primeiro orçamento para começar a controlar seus gastos.</p>
              <button
                onClick={() => setModalAberto(true)}
                className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md"
              >
                Criar Orçamento
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {orcamentos.map((orcamento) => {
                const percentual = calcularPercentual(orcamento.valorAtual, orcamento.valorLimite);
                const corProgresso = getCorProgresso(percentual);
                const restante = orcamento.valorLimite - orcamento.valorAtual;
                
                return (
                  <div key={orcamento.id} className="bg-white border rounded-lg p-6 shadow-sm hover:shadow-md transition-shadow">
                    <div className="flex justify-between items-start mb-4">
                      <div>
                        <h3 className="text-lg font-semibold text-gray-800">{orcamento.nome}</h3>
                        <p className="text-sm text-gray-600">{orcamento.categoria.nome}</p>
                      </div>
                      <span className={`px-2 py-1 text-xs rounded-full ${
                        percentual > 100 ? 'bg-red-100 text-red-800' :
                        percentual > 80 ? 'bg-yellow-100 text-yellow-800' :
                        'bg-green-100 text-green-800'
                      }`}>
                        {percentual.toFixed(1)}%
                      </span>
                    </div>
                    
                    <div className="space-y-3">
                      <div>
                        <div className="flex justify-between text-sm text-gray-600 mb-1">
                          <span>Atual</span>
                          <span>{formatarValor(orcamento.valorAtual)}</span>
                        </div>
                        <div className="w-full bg-gray-200 rounded-full h-2">
                          <div
                            className={`${corProgresso} h-2 rounded-full transition-all duration-300`}
                            style={{ width: `${Math.min(percentual, 100)}%` }}
                          ></div>
                        </div>
                      </div>
                      
                      <div className="flex justify-between text-sm">
                        <span className="text-gray-600">Limite</span>
                        <span className="font-medium">{formatarValor(orcamento.valorLimite)}</span>
                      </div>
                      
                      <div className="flex justify-between text-sm">
                        <span className="text-gray-600">Restante</span>
                        <span className={`font-medium ${
                          restante < 0 ? 'text-red-600' : 
                          restante < orcamento.valorLimite * 0.2 ? 'text-yellow-600' : 
                          'text-green-600'
                        }`}>
                          {formatarValor(restante)}
                        </span>
                      </div>
                      
                      <div className="flex justify-between text-sm">
                        <span className="text-gray-600">Prazo</span>
                        <span>{formatarData(orcamento.prazo)}</span>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      <AdicionarOrcamentoModal
        isOpen={modalAberto}
        onClose={() => setModalAberto(false)}
        onOrcamentoAdicionado={handleOrcamentoAdicionado}
      />
    </div>
  );
};

export default OrcamentoPage;
