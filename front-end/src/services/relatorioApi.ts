import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config: any) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export interface RelatorioMensal {
  mes: number;
  ano: number;
  receitas: number;
  despesas: number;
  saldo: number;
  categorias: {
    nome: string;
    valor: number;
    tipo: 'RECEITA' | 'DESPESA';
  }[];
}

export interface RelatorioCategoria {
  categoria: string;
  total: number;
  tipo: 'RECEITA' | 'DESPESA';
  percentual: number;
}

export interface RelatorioPeriodo {
  dataInicio: string;
  dataFim: string;
  totalReceitas: number;
  totalDespesas: number;
  saldo: number;
  transacoes: {
    data: string;
    descricao: string;
    valor: number;
    categoria: string;
    tipo: 'RECEITA' | 'DESPESA';
  }[];
}

export const relatorioService = {
  // Relatório mensal via Spring Boot (proxy para FastAPI)
  obterRelatorioMensal: async (mes: number, ano: number): Promise<any> => {
    const response = await api.get(`/relatorios/relatorioSaldoMensal?ano=${ano}&formato=excel`, {
      responseType: 'blob'
    });
    return response.data;
  },

  // Relatório por categoria via Spring Boot (proxy para FastAPI)
  obterRelatorioPorCategoria: async (dataInicio: string, dataFim: string): Promise<any> => {
    const response = await api.get(`/relatorios/relatorioPorCategoria?dataInicio=${dataInicio}&dataFim=${dataFim}&formato=excel`, {
      responseType: 'blob'
    });
    return response.data;
  },

  // Relatório de período via Spring Boot (proxy para FastAPI)
  obterRelatorioPeriodo: async (dataInicio: string, dataFim: string): Promise<any> => {
    const response = await api.get(`/relatorios/relatorioTransacaoDetalhado?dataInicio=${dataInicio}&dataFim=${dataFim}&formato=excel`, {
      responseType: 'blob'
    });
    return response.data;
  },

  // Resumo anual via Spring Boot (proxy para FastAPI)
  obterResumoAnual: async (ano: number): Promise<any> => {
    const response = await api.get(`/relatorios/relatorioSaldoMensal?ano=${ano}&formato=excel`, {
      responseType: 'blob'
    });
    return response.data;
  }
};

export default api;
