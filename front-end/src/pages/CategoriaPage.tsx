import React, { useState, useEffect } from 'react';
import { Categoria } from '../types/categoria';
import categoriaApiService from '../services/categoriaApi';
import { orcamentoApiService } from '../services/orcamentoApi';
import { transacaoApiService } from '../services/transacaoApi';
import { Transacao } from '../types/transacao';
import { Orcamento } from '../types/orcamento';

const CategoriaPage: React.FC = () => {
  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [categoriaSelecionada, setCategoriaSelecionada] = useState<Categoria | null>(null);
  const [orcamentos, setOrcamentos] = useState<Orcamento[]>([]);
  const [transacoes, setTransacoes] = useState<Transacao[]>([]);
  const [carregando, setCarregando] = useState<boolean>(true);
  const [erro, setErro] = useState<string>('');
  const [paginaAtualOrcamentos, setPaginaAtualOrcamentos] = useState<number>(0);
  const [totalPaginasOrcamentos, setTotalPaginasOrcamentos] = useState<number>(0);
  const [totalElementosOrcamentos, setTotalElementosOrcamentos] = useState<number>(0);
  const [paginaAtualTransacoes, setPaginaAtualTransacoes] = useState<number>(0);
  const [totalPaginasTransacoes, setTotalPaginasTransacoes] = useState<number>(0);
  const [totalElementosTransacoes, setTotalElementosTransacoes] = useState<number>(0);
  const tamanhoPagina = 5;

  useEffect(() => {
    carregarCategorias();
  }, []);

  const carregarCategorias = async () => {
    try {
      setCarregando(true);
      const categoriasData = await categoriaApiService.listarTodas();
      setCategorias(categoriasData);
    } catch (error: any) {
      console.error('Erro ao carregar categorias:', error);
      setErro('Erro ao carregar categorias. Tente novamente mais tarde.');
    } finally {
      setCarregando(false);
    }
  };

  const handleCategoriaClick = async (categoria: Categoria) => {
    try {
      setCarregando(true);
      setCategoriaSelecionada(categoria);
      setErro('');
      setPaginaAtualOrcamentos(0);
      setPaginaAtualTransacoes(0);

      // Carregar orçamentos da categoria com paginação
      await carregarOrcamentosDaCategoria(categoria.nome, 0);

      // Carregar transações da categoria com paginação
      await carregarTransacoesDaCategoria(categoria.nome, 0);
    } catch (error: any) {
      console.error('Erro ao carregar dados da categoria:', error);
      setErro('Erro ao carregar dados da categoria.');
    } finally {
      setCarregando(false);
    }
  };

  const carregarOrcamentosDaCategoria = async (nomeCategoria: string, pagina: number) => {
    try {
      const response = await orcamentoApiService.obterPorCategoria(nomeCategoria, pagina, tamanhoPagina);
      setOrcamentos(response.content || []);
      setTotalPaginasOrcamentos(response.totalPages || 0);
      setTotalElementosOrcamentos(response.totalElements || 0);
      setPaginaAtualOrcamentos(pagina);
    } catch (error: any) {
      console.error('Erro ao carregar orçamentos:', error);
      setOrcamentos([]);
      setTotalPaginasOrcamentos(0);
      setTotalElementosOrcamentos(0);
    }
  };

  const carregarTransacoesDaCategoria = async (nomeCategoria: string, pagina: number) => {
    try {
      const transacoesResponse = await transacaoApiService.obterPorCategoriaPaginado(nomeCategoria, pagina, tamanhoPagina);
      setTransacoes(transacoesResponse.content || []);
      setTotalPaginasTransacoes(transacoesResponse.totalPages || 0);
      setTotalElementosTransacoes(transacoesResponse.totalElements || 0);
      setPaginaAtualTransacoes(pagina);
    } catch (error: any) {
      console.error('Erro ao carregar transações:', error);
      setTransacoes([]);
      setTotalPaginasTransacoes(0);
      setTotalElementosTransacoes(0);
    }
  };

  const proximaPaginaOrcamentos = () => {
    if (categoriaSelecionada && paginaAtualOrcamentos < totalPaginasOrcamentos - 1) {
      carregarOrcamentosDaCategoria(categoriaSelecionada.nome, paginaAtualOrcamentos + 1);
    }
  };

  const paginaAnteriorOrcamentos = () => {
    if (categoriaSelecionada && paginaAtualOrcamentos > 0) {
      carregarOrcamentosDaCategoria(categoriaSelecionada.nome, paginaAtualOrcamentos - 1);
    }
  };

  const proximaPaginaTransacoes = () => {
    if (categoriaSelecionada && paginaAtualTransacoes < totalPaginasTransacoes - 1) {
      carregarTransacoesDaCategoria(categoriaSelecionada.nome, paginaAtualTransacoes + 1);
    }
  };

  const paginaAnteriorTransacoes = () => {
    if (categoriaSelecionada && paginaAtualTransacoes > 0) {
      carregarTransacoesDaCategoria(categoriaSelecionada.nome, paginaAtualTransacoes - 1);
    }
  };

  const formatarValor = (valor: number): string => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor);
  };

  const formatarData = (data: string): string => {
    return new Date(data).toLocaleDateString('pt-BR');
  };

  const voltarParaCategorias = () => {
    setCategoriaSelecionada(null);
    setOrcamentos([]);
    setTransacoes([]);
    setPaginaAtualOrcamentos(0);
    setTotalPaginasOrcamentos(0);
    setTotalElementosOrcamentos(0);
    setPaginaAtualTransacoes(0);
    setTotalPaginasTransacoes(0);
    setTotalElementosTransacoes(0);
  };

  if (carregando && categorias.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">
            {categoriaSelecionada ? categoriaSelecionada.nome : 'Categorias'}
          </h1>
          {categoriaSelecionada && (
            <button
              onClick={voltarParaCategorias}
              className="mt-2 text-blue-600 hover:text-blue-800 text-sm"
            >
              ← Voltar para categorias
            </button>
          )}
        </div>

        {erro && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
            {erro}
          </div>
        )}

        {!categoriaSelecionada ? (
          // Lista de categorias
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {categorias.map((categoria) => (
              <div
                key={categoria.id}
                onClick={() => handleCategoriaClick(categoria)}
                className="bg-white rounded-lg shadow-md p-6 cursor-pointer hover:shadow-lg transition-shadow"
              >
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-gray-800">{categoria.nome}</h3>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                    categoria.tipo === 'RECEITA' 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-red-100 text-red-800'
                  }`}>
                    {categoria.tipo}
                  </span>
                </div>
                <div className="text-gray-600 text-sm">
                  Clique para ver detalhes
                </div>
              </div>
            ))}
          </div>
        ) : (
          // Detalhes da categoria selecionada
          <div className="space-y-6">
            {/* Orçamentos */}
            <div className="bg-white rounded-lg shadow-md p-6">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold text-gray-800">Orçamentos</h2>
                <div className="text-sm text-gray-500">
                  Mostrando {orcamentos.length} de {totalElementosOrcamentos} orçamentos
                </div>
              </div>
              {orcamentos.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  <p>Nenhum orçamento encontrado para esta categoria.</p>
                </div>
              ) : (
                <>
                  <div className="space-y-4">
                    {orcamentos.map((orcamento) => {
                      const percentual = (orcamento.valorAtual / orcamento.valorLimite) * 100;
                      const corProgresso = percentual < 50 ? 'bg-green-500' : 
                                         percentual < 80 ? 'bg-yellow-500' : 'bg-red-500';
                      
                      return (
                        <div key={orcamento.id} className="border rounded-lg p-4">
                          <div className="flex justify-between items-center mb-2">
                            <h3 className="font-semibold text-gray-800">{orcamento.nome}</h3>
                            <span className="text-sm text-gray-600">
                              {orcamento.prazo ? new Date(orcamento.prazo).toLocaleDateString('pt-BR') : 'Sem prazo'}
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
                          </div>
                        </div>
                      );
                    })}
                  </div>

                  {/* Botões de Paginação dos Orçamentos */}
                  {totalPaginasOrcamentos > 1 && (
                    <div className="flex justify-between items-center mt-6">
                      <button
                        onClick={paginaAnteriorOrcamentos}
                        disabled={paginaAtualOrcamentos === 0}
                        className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        ← Anterior
                      </button>
                      
                      <div className="text-sm text-gray-600">
                        Página {paginaAtualOrcamentos + 1} de {totalPaginasOrcamentos}
                      </div>
                      
                      <button
                        onClick={proximaPaginaOrcamentos}
                        disabled={paginaAtualOrcamentos >= totalPaginasOrcamentos - 1}
                        className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        Próximas {tamanhoPagina} →
                      </button>
                    </div>
                  )}
                </>
              )}
            </div>

            {/* Transações */}
            <div className="bg-white rounded-lg shadow-md p-6">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold text-gray-800">Transações</h2>
                <div className="text-sm text-gray-500">
                  Mostrando {transacoes.length} de {totalElementosTransacoes} transações
                </div>
              </div>
              {transacoes.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  <p>Nenhuma transação encontrada para esta categoria.</p>
                </div>
              ) : (
                <>
                  <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Descrição
                          </th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Valor
                          </th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Tipo
                          </th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Data
                          </th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {transacoes.map((transacao) => (
                          <tr key={transacao.id}>
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                              {transacao.descricao}
                            </td>
                            <td className={`px-6 py-4 whitespace-nowrap text-sm ${
                              transacao.tipo === 'RECEITA' ? 'text-green-600' : 'text-red-600'
                            }`}>
                              {transacao.tipo === 'RECEITA' ? '+' : '-'}{formatarValor(transacao.valor)}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                transacao.tipo === 'RECEITA' 
                                  ? 'bg-green-100 text-green-800' 
                                  : 'bg-red-100 text-red-800'
                              }`}>
                                {transacao.tipo}
                              </span>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                              {formatarData(transacao.date || '')}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {/* Botões de Paginação das Transações */}
                  {totalPaginasTransacoes > 1 && (
                    <div className="flex justify-between items-center mt-6">
                      <button
                        onClick={paginaAnteriorTransacoes}
                        disabled={paginaAtualTransacoes === 0}
                        className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        ← Anterior
                      </button>
                      
                      <div className="text-sm text-gray-600">
                        Página {paginaAtualTransacoes + 1} de {totalPaginasTransacoes}
                      </div>
                      
                      <button
                        onClick={proximaPaginaTransacoes}
                        disabled={paginaAtualTransacoes >= totalPaginasTransacoes - 1}
                        className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        Próximas {tamanhoPagina} →
                      </button>
                    </div>
                  )}
                </>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CategoriaPage;
