import React, { useState, useEffect } from 'react';
import { Transacao, TransacaoTipos, Categoria, Conta, Usuario } from '../types/transacao';
import { transacaoApiService } from '../services/transacaoApi';

interface TransacoesListProps {
  contaId: number;
  contaNome?: string;
  onTransacaoAtualizada?: () => void;
}

const TransacoesList: React.FC<TransacoesListProps> = ({ 
  contaId, 
  contaNome,
  onTransacaoAtualizada 
}) => {
  const [transacoes, setTransacoes] = useState<Transacao[]>([]);
  const [carregando, setCarregando] = useState<boolean>(true);
  const [erro, setErro] = useState<string>('');
  const [paginaAtual, setPaginaAtual] = useState<number>(0);
  const [totalPaginas, setTotalPaginas] = useState<number>(0);
  const [totalElementos, setTotalElementos] = useState<number>(0);
  const tamanhoPagina = 5;

  const carregarTransacoes = async (pagina: number = 0) => {
    try {
      setCarregando(true);
      const response = await transacaoApiService.obterPorContaPaginado(contaNome || '', pagina, tamanhoPagina);
      setTransacoes(response.content || []);
      setTotalPaginas(response.totalPages || 0);
      setTotalElementos(response.totalElements || 0);
      setPaginaAtual(pagina);
    } catch (error: any) {
      console.error('Erro ao carregar transações:', error);
      setErro('Erro ao carregar transações. Tente novamente mais tarde.');
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    carregarTransacoes();
  }, [contaId]);

  useEffect(() => {
    if (onTransacaoAtualizada) {
      onTransacaoAtualizada();
    }
  }, [transacoes, onTransacaoAtualizada]);

  const proximaPagina = () => {
    if (paginaAtual < totalPaginas - 1) {
      carregarTransacoes(paginaAtual + 1);
    }
  };

  const paginaAnterior = () => {
    if (paginaAtual > 0) {
      carregarTransacoes(paginaAtual - 1);
    }
  };

  const formatarData = (dataString?: string): string => {
    if (!dataString) return '-';
    const data = new Date(dataString);
    return data.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatarValor = (valor: number, tipo: TransacaoTipos): string => {
    const formatted = new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(Math.abs(valor));
    
    return tipo === TransacaoTipos.DESPESA ? `-${formatted}` : `+${formatted}`;
  };

  const getCorValor = (tipo: TransacaoTipos): string => {
    return tipo === TransacaoTipos.DESPESA ? 'text-red-600' : 'text-green-600';
  };

  const getCorBadge = (tipo: TransacaoTipos): string => {
    return tipo === TransacaoTipos.DESPESA 
      ? 'bg-red-100 text-red-800' 
      : 'bg-green-100 text-green-800';
  };

  const getTipoLabel = (tipo: TransacaoTipos): string => {
    return tipo === TransacaoTipos.DESPESA ? 'Despesa' : 'Receita';
  };

  if (carregando) {
    return (
      <div className="flex justify-center items-center h-32">
        <div className="text-gray-500">Carregando transações...</div>
      </div>
    );
  }

  if (erro) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
        {erro}
      </div>
    );
  }

  if (transacoes.length === 0) {
    return (
      <div className="text-center py-8">
        <div className="text-gray-500">Nenhuma transação encontrada</div>
        <p className="text-sm text-gray-400 mt-2">
          Adicione sua primeira transação usando o botão "Adicionar Transação"
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold text-gray-900">Transações Recentes</h3>
        <div className="text-sm text-gray-500">
          Mostrando {transacoes.length} de {totalElementos} transações
        </div>
      </div>
      
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Data
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Descrição
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Categoria
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Tipo
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Valor
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {transacoes.map((transacao) => (
                <tr key={transacao.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {formatarData(transacao.date)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {transacao.descricao}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {transacao.categoria?.nome || '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getCorBadge(transacao.tipo)}`}>
                      {getTipoLabel(transacao.tipo)}
                    </span>
                  </td>
                  <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium text-right ${getCorValor(transacao.tipo)}`}>
                    {formatarValor(transacao.valor, transacao.tipo)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      
      
      {totalPaginas > 1 && (
        <div className="flex justify-between items-center mt-4">
          <div className="text-sm text-gray-500">
            Página {paginaAtual + 1} de {totalPaginas}
          </div>
          <div className="flex space-x-2">
            <button
              onClick={paginaAnterior}
              disabled={paginaAtual === 0}
              className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
            >
              ← Anterior
            </button>
            <button
              onClick={proximaPagina}
              disabled={paginaAtual === totalPaginas - 1}
              className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
            >
              Próximas 5 →
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default TransacoesList;
