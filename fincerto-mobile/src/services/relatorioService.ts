import api from './api';

export const relatorioService = {
  obterRelatorioMensal: async (mes: number, ano: number): Promise<any> => {
    const response = await api.get(`/relatorios/relatorioSaldoMensal?ano=${ano}&formato=excel`, {
      responseType: 'blob' as any,
    });
    return response.data;
  },

  obterRelatorioPorCategoria: async (dataInicio: string, dataFim: string, categoriaId?: string): Promise<any> => {
    let url = `/relatorios/relatorioPorCategoria?dataInicio=${dataInicio}&dataFim=${dataFim}&formato=excel`;
    if (categoriaId) url += `&categoriaId=${categoriaId}`;
    const response = await api.get(url, { responseType: 'blob' as any });
    return response.data;
  },

  obterRelatorioPeriodo: async (dataInicio: string, dataFim: string): Promise<any> => {
    const response = await api.get(`/relatorios/relatorioTransacaoDetalhado?dataInicio=${dataInicio}&dataFim=${dataFim}&formato=excel`, { responseType: 'blob' as any });
    return response.data;
  },

  obterResumoAnual: async (ano: number): Promise<any> => {
    const response = await api.get(`/relatorios/relatorioSaldoMensal?ano=${ano}&formato=excel`, { responseType: 'blob' as any });
    return response.data;
  },
};

export default relatorioService;
