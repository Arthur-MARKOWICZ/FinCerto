import React, { useState, useEffect } from 'react';
import { relatorioService, RelatorioMensal, RelatorioCategoria, RelatorioPeriodo } from '../services/relatorioApi';
import { categoriaService } from '../services/api';
import { Categoria } from '../types/categoria';

const RelatorioPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'mensal' | 'categoria' | 'periodo' | 'anual'>('mensal');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Estados para relatório mensal
  const [mes, setMes] = useState(new Date().getMonth() + 1);
  const [ano, setAno] = useState(new Date().getFullYear());
  const [relatorioMensal, setRelatorioMensal] = useState<RelatorioMensal | null>(null);

  // Estados para relatório por categoria
  const [dataInicio, setDataInicio] = useState(new Date().toISOString().split('T')[0]);
  const [dataFim, setDataFim] = useState(new Date().toISOString().split('T')[0]);
  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [categoriaSelecionada, setCategoriaSelecionada] = useState<string>('');
  const [relatorioCategoria, setRelatorioCategoria] = useState<RelatorioCategoria[]>([]);

  // Estados para relatório de período
  const [relatorioPeriodo, setRelatorioPeriodo] = useState<RelatorioPeriodo | null>(null);

  // Estados para resumo anual
  const [anoResumo, setAnoResumo] = useState(new Date().getFullYear());
  const [resumoAnual, setResumoAnual] = useState<any>(null);

  const carregarRelatorioMensal = async () => {
    setLoading(true);
    setError(null);
    try {
      const blob = await relatorioService.obterRelatorioMensal(mes, ano);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `relatorio_mensal_${mes}_${ano}.xlsx`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      setRelatorioMensal(null); // Limpar dados pois estamos baixando arquivo
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao baixar relatório mensal');
    } finally {
      setLoading(false);
    }
  };

  const carregarRelatorioCategoria = async () => {
    setLoading(true);
    setError(null);
    try {
      const blob = await relatorioService.obterRelatorioPorCategoria(dataInicio, dataFim, categoriaSelecionada || undefined);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      const categoriaNome = categoriaSelecionada ? categorias.find(c => c.id?.toString() === categoriaSelecionada)?.nome || categoriaSelecionada : 'todas-categorias';
      a.download = `relatorio_categorias_${categoriaNome}_${dataInicio}_${dataFim}.xlsx`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      setRelatorioCategoria([]); // Limpar dados pois estamos baixando arquivo
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao baixar relatório por categoria');
    } finally {
      setLoading(false);
    }
  };

  const carregarRelatorioPeriodo = async () => {
    setLoading(true);
    setError(null);
    try {
      const blob = await relatorioService.obterRelatorioPeriodo(dataInicio, dataFim);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `relatorio_periodo_${dataInicio}_${dataFim}.xlsx`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      setRelatorioPeriodo(null); // Limpar dados pois estamos baixando arquivo
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao baixar relatório de período');
    } finally {
      setLoading(false);
    }
  };

  const carregarResumoAnual = async () => {
    setLoading(true);
    setError(null);
    try {
      const blob = await relatorioService.obterResumoAnual(anoResumo);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `relatorio_anual_${anoResumo}.xlsx`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      setResumoAnual(null); // Limpar dados pois estamos baixando arquivo
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao baixar resumo anual');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // Não carregar automaticamente, apenas quando o usuário clicar no botão
    carregarCategorias();
  }, []);

  const carregarCategorias = async () => {
    try {
      const categoriasData = await categoriaService.listarTodas();
      setCategorias(categoriasData);
    } catch (err: any) {
      console.error('Erro ao carregar categorias:', err);
    }
  };

  const formatarMoeda = (valor: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor);
  };

  const getNomeMes = (mes: number) => {
    const meses = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
    return meses[mes - 1];
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="bg-white shadow rounded-lg">
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8 px-6 pt-4" aria-label="Tabs">
            {(['mensal', 'categoria', 'periodo', 'anual'] as const).map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`${
                  activeTab === tab
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                } whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm capitalize`}
              >
                {tab === 'mensal' && 'Relatório Mensal'}
                {tab === 'categoria' && 'Por Categoria'}
                {tab === 'periodo' && 'Por Período'}
                {tab === 'anual' && 'Resumo Anual'}
              </button>
            ))}
          </nav>
        </div>

        <div className="p-6">
          <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <p className="text-sm text-blue-800">
                  <strong>Relatórios em Excel:</strong> Os relatórios são gerados via FastAPI e baixados como arquivos Excel (.xlsx) para análise detalhada.
                </p>
              </div>
            </div>
          </div>

          {error && (
            <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
              {error}
            </div>
          )}

          {loading && (
            <div className="flex justify-center items-center py-8">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            </div>
          )}

          {/* Relatório Mensal */}
          {activeTab === 'mensal' && !loading && (
            <div>
              <div className="mb-6 flex gap-4 items-end">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Mês</label>
                  <select
                    value={mes}
                    onChange={(e) => setMes(Number(e.target.value))}
                    className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                    {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                      <option key={m} value={m}>{getNomeMes(m)}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Ano</label>
                  <input
                    type="number"
                    value={ano}
                    onChange={(e) => setAno(Number(e.target.value))}
                    className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <button
                  onClick={carregarRelatorioMensal}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                >
                  📥 Baixar Relatório Excel
                </button>
              </div>

              {relatorioMensal && (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className="bg-green-50 p-4 rounded-lg">
                      <h3 className="text-sm font-medium text-green-800">Receitas</h3>
                      <p className="text-2xl font-bold text-green-600">{formatarMoeda(relatorioMensal.receitas)}</p>
                    </div>
                    <div className="bg-red-50 p-4 rounded-lg">
                      <h3 className="text-sm font-medium text-red-800">Despesas</h3>
                      <p className="text-2xl font-bold text-red-600">{formatarMoeda(relatorioMensal.despesas)}</p>
                    </div>
                    <div className={`${relatorioMensal.saldo >= 0 ? 'bg-blue-50' : 'bg-orange-50'} p-4 rounded-lg`}>
                      <h3 className={`text-sm font-medium ${relatorioMensal.saldo >= 0 ? 'text-blue-800' : 'text-orange-800'}`}>Saldo</h3>
                      <p className={`text-2xl font-bold ${relatorioMensal.saldo >= 0 ? 'text-blue-600' : 'text-orange-600'}`}>
                        {formatarMoeda(relatorioMensal.saldo)}
                      </p>
                    </div>
                  </div>

                  <div>
                    <h3 className="text-lg font-medium text-gray-900 mb-4">Resumo por Categoria</h3>
                    <div className="space-y-2">
                      {relatorioMensal.categorias.map((cat, index) => (
                        <div key={index} className="flex justify-between items-center p-3 bg-gray-50 rounded">
                          <span className="font-medium">{cat.nome}</span>
                          <span className={`${cat.tipo === 'RECEITA' ? 'text-green-600' : 'text-red-600'} font-bold`}>
                            {formatarMoeda(cat.valor)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Relatório por Categoria */}
          {activeTab === 'categoria' && !loading && (
            <div>
              <div className="mb-6">
                {categoriaSelecionada && (
                  <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                    <p className="text-sm text-blue-800">
                      <strong>Filtro aplicado:</strong> Relatório será gerado apenas para a categoria "{categorias.find(c => c.id?.toString() === categoriaSelecionada)?.nome || categoriaSelecionada}"
                    </p>
                  </div>
                )}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Data Início</label>
                    <input
                      type="date"
                      value={dataInicio}
                      onChange={(e) => setDataInicio(e.target.value)}
                      className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Data Fim</label>
                    <input
                      type="date"
                      value={dataFim}
                      onChange={(e) => setDataFim(e.target.value)}
                      className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Categoria (Opcional)</label>
                    <select
                      value={categoriaSelecionada}
                      onChange={(e) => setCategoriaSelecionada(e.target.value)}
                      className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    >
                      <option value="">Todas as categorias</option>
                      {categorias.map((cat) => (
                        <option key={cat.id || cat.nome} value={cat.id?.toString() || cat.nome}>
                          {cat.nome} ({cat.tipo})
                        </option>
                      ))}
                    </select>
                  </div>
                  <button
                    onClick={carregarRelatorioCategoria}
                    className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                  >
                    📥 Baixar Relatório Excel
                  </button>
                </div>
              </div>

              {relatorioCategoria.length > 0 && (
                <div className="space-y-2">
                  {relatorioCategoria.map((cat, index) => (
                    <div key={index} className="flex justify-between items-center p-4 bg-gray-50 rounded-lg">
                      <div>
                        <span className="font-medium text-gray-900">{cat.categoria}</span>
                        <span className={`ml-2 text-xs px-2 py-1 rounded ${cat.tipo === 'RECEITA' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                          {cat.tipo}
                        </span>
                      </div>
                      <div className="text-right">
                        <p className={`font-bold ${cat.tipo === 'RECEITA' ? 'text-green-600' : 'text-red-600'}`}>
                          {formatarMoeda(cat.total)}
                        </p>
                        <p className="text-sm text-gray-500">{cat.percentual.toFixed(1)}%</p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Relatório de Período */}
          {activeTab === 'periodo' && !loading && (
            <div>
              <div className="mb-6 flex gap-4 items-end">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Data Início</label>
                  <input
                    type="date"
                    value={dataInicio}
                    onChange={(e) => setDataInicio(e.target.value)}
                    className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Data Fim</label>
                  <input
                    type="date"
                    value={dataFim}
                    onChange={(e) => setDataFim(e.target.value)}
                    className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <button
                  onClick={carregarRelatorioPeriodo}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                >
                  📥 Baixar Relatório Excel
                </button>
              </div>

              {relatorioPeriodo && (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className="bg-green-50 p-4 rounded-lg">
                      <h3 className="text-sm font-medium text-green-800">Total Receitas</h3>
                      <p className="text-2xl font-bold text-green-600">{formatarMoeda(relatorioPeriodo.totalReceitas)}</p>
                    </div>
                    <div className="bg-red-50 p-4 rounded-lg">
                      <h3 className="text-sm font-medium text-red-800">Total Despesas</h3>
                      <p className="text-2xl font-bold text-red-600">{formatarMoeda(relatorioPeriodo.totalDespesas)}</p>
                    </div>
                    <div className={`${relatorioPeriodo.saldo >= 0 ? 'bg-blue-50' : 'bg-orange-50'} p-4 rounded-lg`}>
                      <h3 className={`text-sm font-medium ${relatorioPeriodo.saldo >= 0 ? 'text-blue-800' : 'text-orange-800'}`}>Saldo</h3>
                      <p className={`text-2xl font-bold ${relatorioPeriodo.saldo >= 0 ? 'text-blue-600' : 'text-orange-600'}`}>
                        {formatarMoeda(relatorioPeriodo.saldo)}
                      </p>
                    </div>
                  </div>

                  <div>
                    <h3 className="text-lg font-medium text-gray-900 mb-4">Transações</h3>
                    <div className="overflow-x-auto">
                      <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                          <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Data</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Descrição</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Categoria</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Valor</th>
                          </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                          {relatorioPeriodo.transacoes.map((transacao, index) => (
                            <tr key={index}>
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                {new Date(transacao.data).toLocaleDateString('pt-BR')}
                              </td>
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{transacao.descricao}</td>
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{transacao.categoria}</td>
                              <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium ${transacao.tipo === 'RECEITA' ? 'text-green-600' : 'text-red-600'}`}>
                                {formatarMoeda(transacao.valor)}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Resumo Anual */}
          {activeTab === 'anual' && !loading && (
            <div>
              <div className="mb-6 flex gap-4 items-end">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Ano</label>
                  <input
                    type="number"
                    value={anoResumo}
                    onChange={(e) => setAnoResumo(Number(e.target.value))}
                    className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <button
                  onClick={carregarResumoAnual}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                >
                  📥 Baixar Relatório Excel
                </button>
              </div>

              {resumoAnual && (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className="bg-green-50 p-4 rounded-lg">
                      <h3 className="text-sm font-medium text-green-800">Total Receitas Ano</h3>
                      <p className="text-2xl font-bold text-green-600">{formatarMoeda(resumoAnual.totalReceitas)}</p>
                    </div>
                    <div className="bg-red-50 p-4 rounded-lg">
                      <h3 className="text-sm font-medium text-red-800">Total Despesas Ano</h3>
                      <p className="text-2xl font-bold text-red-600">{formatarMoeda(resumoAnual.totalDespesas)}</p>
                    </div>
                    <div className={`${resumoAnual.saldo >= 0 ? 'bg-blue-50' : 'bg-orange-50'} p-4 rounded-lg`}>
                      <h3 className={`text-sm font-medium ${resumoAnual.saldo >= 0 ? 'text-blue-800' : 'text-orange-800'}`}>Saldo Anual</h3>
                      <p className={`text-2xl font-bold ${resumoAnual.saldo >= 0 ? 'text-blue-600' : 'text-orange-600'}`}>
                        {formatarMoeda(resumoAnual.saldo)}
                      </p>
                    </div>
                  </div>

                  <div>
                    <h3 className="text-lg font-medium text-gray-900 mb-4">Resumo Mensal</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                      {resumoAnual.meses.map((mesData: RelatorioMensal, index: number) => (
                        <div key={index} className="border border-gray-200 rounded-lg p-4">
                          <h4 className="font-medium text-gray-900 mb-2">{getNomeMes(mesData.mes)}</h4>
                          <div className="space-y-1 text-sm">
                            <div className="flex justify-between">
                              <span>Receitas:</span>
                              <span className="text-green-600">{formatarMoeda(mesData.receitas)}</span>
                            </div>
                            <div className="flex justify-between">
                              <span>Despesas:</span>
                              <span className="text-red-600">{formatarMoeda(mesData.despesas)}</span>
                            </div>
                            <div className="flex justify-between font-medium border-t pt-1">
                              <span>Saldo:</span>
                              <span className={mesData.saldo >= 0 ? 'text-blue-600' : 'text-orange-600'}>
                                {formatarMoeda(mesData.saldo)}
                              </span>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default RelatorioPage;
