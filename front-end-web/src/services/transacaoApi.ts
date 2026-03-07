import axios from 'axios';
import { TransacaoCadastroDto, Transacao } from '../types/transacao';

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

export const transacaoApiService = {
  cadastrar: async (dados: TransacaoCadastroDto): Promise<Transacao> => {
    const response = await api.post<Transacao>('/transacao', dados);
    return response.data;
  },

  obterPorId: async (id: number): Promise<Transacao> => {
    const response = await api.get<Transacao>(`/transacao/${id}`);
    return response.data;
  },

  obterPorConta: async (contaId: number): Promise<Transacao[]> => {
    const response = await api.get<Transacao[]>(`/transacao/conta/${contaId}`);
    return response.data;
  },

  obterPorContaPaginado: async (nomeConta: string, pagina: number = 0, tamanho: number = 10): Promise<any> => {
    const response = await api.get(`/transacao/conta/${nomeConta}/paginado?pagina=${pagina}&tamanho=${tamanho}`);
    return response.data;
  },

  obterPorCategoriaPaginado: async (nomeCategoria: string, pagina: number = 0, tamanho: number = 10): Promise<any> => {
    const response = await api.get(`/transacao/categoria/${nomeCategoria}/paginado?pagina=${pagina}&tamanho=${tamanho}`);
    return response.data;
  },
};

export default api;
