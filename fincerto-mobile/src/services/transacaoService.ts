import { TransacaoCadastroDto, Transaction } from '../types/transaction';
import api from './api';

export const transacaoService = {
  criar: async (dados: TransacaoCadastroDto): Promise<Transaction> => {
    const response = await api.post<Transaction>('/transacao', dados);
    return response.data;
  },

  listar: async (): Promise<Transaction[]> => {
    const response = await api.get<{ content: Transaction[] }>('/transacao?pagina=0&tamanho=10');
    return response.data.content || [];
  },

  listarPorConta: async (contaId: number): Promise<Transaction[]> => {
    const response = await api.get<Transaction[]>(`/transacao/conta/${contaId}`);
    return response.data;
  },

  atualizar: async (id: number, dados: Partial<Transaction>): Promise<Transaction> => {
    const response = await api.put<Transaction>(`/transacao/${id}`, dados);
    return response.data;
  },

  deletar: async (id: number): Promise<void> => {
    await api.delete(`/transacao/${id}`);
  },
};

export default transacaoService;
