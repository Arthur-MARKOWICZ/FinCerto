import api from './api';

export const relatorioService = {
  obterRelatorioMensal: async (ano: number, formato: string = 'excel'): Promise<any> => {
    const response = await api.get(`/relatorios/relatorioSaldoMensal?formato=${encodeURIComponent(formato)}&ano=${encodeURIComponent(String(ano))}`, {
      responseType: 'arraybuffer' as any,
    });
    return response.data;
  },

  /**
   * Chama /api/relatorios/relatorioPorCategoria
   * Parâmetros esperados pelo backend: formato (default=excel), tipo (RECEITA|DESPESA) ou categoriaId
   */
  obterRelatorioPorCategoria: async (options: { formato?: string; tipo?: string; categoriaId?: string } = {}): Promise<any> => {
    const { formato = 'excel', tipo, categoriaId } = options;
    let url = `/relatorios/relatorioPorCategoria?formato=${encodeURIComponent(formato)}`;
    if (categoriaId) url += `&categoriaId=${encodeURIComponent(categoriaId)}`;
    if (tipo) url += `&tipo=${encodeURIComponent(tipo)}`;
    const response = await api.get(url, { responseType: 'arraybuffer' as any });
    return response.data;
  },

  /**
   * Chama /api/relatorios/relatorioTransacaoDetalhado
   * Parâmetros: formato (default=excel), dataInicio (YYYY-MM-DD), dataFim (YYYY-MM-DD)
   */
  obterRelatorioPeriodo: async (dataInicio?: string, dataFim?: string, formato: string = 'excel'): Promise<any> => {
    let url = `/relatorios/relatorioTransacaoDetalhado?formato=${encodeURIComponent(formato)}`;
    if (dataInicio) url += `&dataInicio=${encodeURIComponent(dataInicio)}`;
    if (dataFim) url += `&dataFim=${encodeURIComponent(dataFim)}`;
    const response = await api.get(url, { responseType: 'arraybuffer' as any });
    return response.data;
  },

  obterResumoAnual: async (ano: number, formato: string = 'excel'): Promise<any> => {
    const response = await api.get(`/relatorios/relatorioSaldoMensal?formato=${encodeURIComponent(formato)}&ano=${encodeURIComponent(String(ano))}`, { responseType: 'arraybuffer' as any });
    return response.data;
  },
};

export default relatorioService;
