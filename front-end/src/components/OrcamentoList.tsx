import React, { useState, useEffect } from 'react';
import { Orcamento } from '../types/orcamento';
import { orcamentoApiService } from '../services/orcamentoApi';

interface OrcamentoListProps {
  onOrcamentoAtualizado?: () => void;
}

const OrcamentoList: React.FC<OrcamentoListProps> = ({ onOrcamentoAtualizado }) => {
  const [orcamentos, setOrcamentos] = useState<Orcamento[]>([]);
  const [carregando, setCarregando] = useState<boolean>(true);
  const [erro, setErro] = useState<string>('');
  const [paginaAtual, setPaginaAtual] = useState<number>(0);
  const [totalPaginas, setTotalPaginas] = useState<number>(0);
  const [totalElementos, setTotalElementos] = useState<number>(0);
  const tamanhoPagina = 5;

  const carregarOrcamentos = async (pagina: number = 0) => {
    try {
      setCarregando(true);
      const response = await orcamentoApiService.obterPorUsuarioPaginado(pagina, tamanhoPagina);
      setOrcamentos(response.content || []);
      setTotalPaginas(response.totalPages || 0);
      setTotalElementos(response.totalElements || 0);
      setPaginaAtual(pagina);
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

  const proximaPagina = () => {
    if (paginaAtual < totalPaginas - 1) {
      carregarOrcamentos(paginaAtual + 1);
    }
  };

  const paginaAnterior = () => {
    if (paginaAtual > 0) {
      carregarOrcamentos(paginaAtual - 1);
    }
  };

  const formatarValor = (valor: number): string => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor);
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

  if (carregando) {
    return (
      <div className="flex justify-center items-center h-32">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (erro) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        {erro}
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold text-gray-800">Orçamentos</h2>
        <div className="text-sm text-gray-500">
          Mostrando {orcamentos.length} de {totalElementos} orçamentos
        </div>
      </div>
      
      {orcamentos.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          <p>Nenhum orçamento cadastrado.</p>
          <p className="text-sm mt-2">Crie orçamentos para controlar seus gastos por categoria.</p>
        </div>
      ) : (
        <>
          <div className="space-y-4">
            {orcamentos.map((orcamento) => {
              const percentual = calcularPercentual(orcamento.valorAtual, orcamento.valorLimite);
              const corProgresso = getCorProgresso(percentual);
              
              return (
                <div key={orcamento.id} className="border rounded-lg p-4">
                  <div className="flex justify-between items-center mb-2">
                    <h3 className="font-semibold text-gray-800">
                      {orcamento.nome}
                    </h3>
                    <span className="text-sm text-gray-600">
                      {orcamento.categoria.nome}
                    </span>
                  </div>
                  
                  <div className="mb-3">
                    <div className="flex justify-between text-sm text-gray-600 mb-1">
                      <span>Atual: {formatarValor(orcamento.valorAtual)}</span>
                      <span>Limite: {formatarValor(orcamento.valorLimite)}</span>
                    </div>
                    
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div
                        className={`${corProgresso} h-2 rounded-full transition-all duration-300`}
                        style={{ width: `${Math.min(percentual, 100)}%` }}
                      ></div>
                    </div>
                    
                    <div className="text-xs text-gray-500 mt-1">
                      {percentual.toFixed(1)}% do limite utilizado
                    </div>
                  </div>
                  
                  <div className="flex justify-between items-center">
                    <span className={`text-sm font-medium ${
                      percentual > 100 ? 'text-red-600' : 
                      percentual > 80 ? 'text-yellow-600' : 
                      'text-green-600'
                    }`}>
                      {formatarValor(orcamento.valorLimite - orcamento.valorAtual)} restantes
                    </span>
                    <span className="text-xs text-gray-500">
                      Prazo: {orcamento.prazo ? new Date(orcamento.prazo).toLocaleDateString('pt-BR') : 'Não definido'}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>

          
          {totalPaginas > 1 && (
            <div className="flex justify-between items-center mt-6">
              <button
                onClick={paginaAnterior}
                disabled={paginaAtual === 0}
                className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                ← Anterior
              </button>
              
              <div className="text-sm text-gray-600">
                Página {paginaAtual + 1} de {totalPaginas}
              </div>
              
              <button
                onClick={proximaPagina}
                disabled={paginaAtual >= totalPaginas - 1}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Próximas {tamanhoPagina} →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default OrcamentoList;
