import axios from 'axios';
import { Orcamento, OrcamentoCadastroDto } from '../types/orcamento';

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

export const orcamentoApiService = {
  cadastrar: async (dados: OrcamentoCadastroDto): Promise<Orcamento> => {
    const response = await api.post<Orcamento>('/orcamentos', dados);
    return response.data;
  },

  obterPorNome: async (nome: string): Promise<Orcamento> => {
    const response = await api.get<Orcamento>(`/orcamentos/nome/${nome}`);
    return response.data;
  },

  obterPorUsuarioPaginado: async (pagina: number = 0, tamanho: number = 5): Promise<any> => {
    const response = await api.get(`/orcamentos/usuario/paginado?pagina=${pagina}&tamanho=${tamanho}`);
    return response.data;
  },

  obterPorCategoria: async (nomeCategoria: string, pagina: number = 0, tamanho: number = 5): Promise<any> => {
    const response = await api.get(`/orcamentos/categoria/${nomeCategoria}?pagina=${pagina}&tamanho=${tamanho}`);
    return response.data;
  },
};

export default api;
