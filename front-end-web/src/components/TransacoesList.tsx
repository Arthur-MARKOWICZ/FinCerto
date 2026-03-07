import React, { useState, useEffect } from 'react';
import { Transacao, TransacaoTipos } from '../types/transacao';
import { transacaoApiService } from '../services/transacaoApi';
import { Loader2, ChevronLeft, ChevronRight } from 'lucide-react';
import { formatCurrency } from '../utils/format';

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
  }, [contaId, contaNome]); // added contaNome to deps

  useEffect(() => {
    if (onTransacaoAtualizada) {
      onTransacaoAtualizada();
    }
  }, [transacoes]);

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

  const formataTransacaoValor = (valor: number, tipo: TransacaoTipos): string => {
    const formatted = formatCurrency(Math.abs(valor));
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
      <div className="flex flex-col justify-center items-center h-32 space-y-2">
        <Loader2 className="animate-spin text-blue-500" size={32} />
        <div className="text-gray-500 font-medium">Carregando transações...</div>
      </div>
    );
  }

  if (erro) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg shadow-sm">
        {erro}
      </div>
    );
  }

  if (transacoes.length === 0) {
    return (
      <div className="text-center bg-gray-50 border border-dashed border-gray-300 rounded-xl py-12">
        <div className="text-gray-500 font-medium">Nenhuma transação encontrada</div>
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
        <div className="text-sm bg-gray-100 px-3 py-1 rounded-full text-gray-600 font-medium shadow-sm">
          {totalElementos} {totalElementos === 1 ? 'transação' : 'transações'}
        </div>
      </div>
      
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Data
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Descrição
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Categoria
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Tipo
                </th>
                <th className="px-6 py-4 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Valor
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-100">
              {transacoes.map((transacao) => (
                <tr key={transacao.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600 font-medium">
                    {formatarData(transacao.date)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-medium">
                    {transacao.descricao}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                    <span className="bg-gray-100 px-2 py-1 rounded-md">
                      {transacao.categoria?.nome || '-'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 py-1 inline-flex text-xs leading-5 font-bold rounded-md shadow-sm ${getCorBadge(transacao.tipo)}`}>
                      {getTipoLabel(transacao.tipo)}
                    </span>
                  </td>
                  <td className={`px-6 py-4 whitespace-nowrap text-sm font-bold text-right ${getCorValor(transacao.tipo)}`}>
                    {formataTransacaoValor(transacao.valor, transacao.tipo)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      
      {totalPaginas > 1 && (
        <div className="flex justify-between items-center mt-6 bg-white p-4 rounded-xl shadow-sm border border-gray-100">
          <div className="text-sm font-medium text-gray-500">
            Página <span className="text-gray-900">{paginaAtual + 1}</span> de <span className="text-gray-900">{totalPaginas}</span>
          </div>
          <div className="flex space-x-2">
            <button
              onClick={paginaAnterior}
              disabled={paginaAtual === 0}
              className="flex items-center px-4 py-2 bg-white text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 hover:text-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-all font-medium shadow-sm"
            >
              <ChevronLeft size={18} className="mr-1" />
              <span>Anterior</span>
            </button>
            <button
              onClick={proximaPagina}
              disabled={paginaAtual === totalPaginas - 1}
              className="flex items-center px-4 py-2 bg-white text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 hover:text-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-all font-medium shadow-sm"
            >
              <span>Próximos</span>
              <ChevronRight size={18} className="ml-1" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default TransacoesList;
