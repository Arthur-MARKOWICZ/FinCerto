import { ContaCadastroDto, Account } from '../types/account';
import { storageService } from './storage';
import api from './api';

export const contaService = {
  criar: async (dados: ContaCadastroDto): Promise<Account> => {
    try {
      const response = await api.post<Account>('/contas', dados);
      return response.data;
    } catch (error: any) {
      console.error('contaService.criar erro', error.response?.data || error.message);
      throw error;
    }
  },

  listar: async (): Promise<Account[]> => {
    const token = await storageService.getToken();
    if (!token) {
      console.warn('contaService.listar: token não encontrado');
      throw new Error('Token não encontrado');
    }
    const response = await api.get<{ content: Account[] }>(`/contas/obterPorUsuario/${token}?page=0&size=100`);
    return response.data.content;
  },

  obterPorId: async (id: number): Promise<Account> => {
    const response = await api.get<Account>(`/contas/nome/${id}`);
    return response.data;
  },

  obterSaldo: async (id: number): Promise<number> => {
    const response = await api.get<number>(`/contas/saldo/${id}`);
    return response.data;
  },

  atualizar: async (id: number, dados: Partial<ContaCadastroDto>): Promise<Account> => {
    const response = await api.post<Account>(`/contas/alterarDadosConta?id=${id}`, dados);
    return response.data;
  },

  deletar: async (id: number): Promise<void> => {
    await api.delete(`/contas/${id}`);
  },
};

export default contaService;
