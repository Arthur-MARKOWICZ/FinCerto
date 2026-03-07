import axios from 'axios';
import { UsuarioCadastroDto, LoginRequestDto, LoginResponseDto, Usuario } from '../types/usuario';
import { ContaCadastroDto, Conta, Tipos } from '../types/conta';
import { TransacaoCadastroDto, Transacao, TransacaoTipos } from '../types/transacao';
import { CategoriaCadastroDto, Categoria } from '../types/categoria';

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

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const usuarioService = {
  cadastrar: async (dados: UsuarioCadastroDto): Promise<Usuario> => {
    const response = await api.post<Usuario>('/usuarios', dados);
    return response.data;
  },

  login: async (dados: LoginRequestDto): Promise<LoginResponseDto> => {
    const response = await api.post<LoginResponseDto>('/usuarios/login', dados);
    return response.data;
  },
};

export const contaService = {
  cadastrar: async (dados: ContaCadastroDto): Promise<Conta> => {
    const response = await api.post<Conta>('/contas', dados);
    return response.data;
  },

  obterPorUsuario: async (token: string, page: number = 0, size: number = 10): Promise<{ content: Conta[] }> => {
    const response = await api.get(`/contas/obterPorUsuario/${token}?page=${page}&size=${size}`);
    return response.data;
  },

  obterPorNome: async (nome: string): Promise<Conta> => {
    const response = await api.get<Conta>(`/contas/nome/${nome}`);
    return response.data;
  },

  obterSaldo: async (id: number): Promise<number> => {
    const response = await api.get<number>(`/contas/saldo/${id}`);
    return response.data;
  },

  atualizar: async (id: number, dados: ContaCadastroDto): Promise<Conta> => {
    const response = await api.post<Conta>(`/contas/alterarDadosConta?id=${id}`, dados);
    return response.data;
  },
};

export const transacaoService = {
  cadastrar: async (dados: TransacaoCadastroDto): Promise<Transacao> => {
    const response = await api.post<Transacao>('/transacao', dados);
    return response.data;
  },

  obterPorId: async (id: number): Promise<Transacao> => {
    const response = await api.get<Transacao>(`/transacao/${id}`);
    return response.data;
  },
};

export const categoriaService = {
  cadastrar: async (dados: CategoriaCadastroDto): Promise<Categoria> => {
    const response = await api.post<Categoria>('/categorias', dados);
    return response.data;
  },

  obterPorNome: async (nome: string): Promise<Categoria> => {
    const response = await api.get<Categoria>(`/categorias/nome/${nome}`);
    return response.data;
  },

  listarTodas: async (): Promise<Categoria[]> => {
    const response = await api.get<{content: Categoria[]}>('/categorias/usuario');
    return response.data.content;
  },
};

export default api;
