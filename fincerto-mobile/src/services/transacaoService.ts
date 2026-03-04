import { TransacaoCadastroDto, Transaction } from '../types/transaction';
import api from './api';

/**
 * transacaoController endpoints (back-end):
 * - POST /api/transacao
 * - GET  /api/transacao/conta/{nomeConta}/paginado?pagina=&tamanho=
 * - GET  /api/transacao/categoria/{nomeCategoria}/paginado?pagina=&tamanho=
 * - GET  /api/transacao/{id}
 */

export const transacaoService = {
  criar: async (dados: TransacaoCadastroDto): Promise<Transaction> => {
    const response = await api.post<Transaction>('/transacao', dados);
    return response.data;
  },

  // retorna a página completa (Page<Transacao>) conforme backend
  listarPorContaPaginado: async (nomeConta: string, pagina = 0, tamanho = 10): Promise<any> => {
    const encoded = encodeURIComponent(nomeConta);
    const response = await api.get(`/transacao/conta/${encoded}/paginado?pagina=${pagina}&tamanho=${tamanho}`);
    return response.data;
  },

  listarPorCategoriaPaginado: async (nomeCategoria: string, pagina = 0, tamanho = 10): Promise<any> => {
    const encoded = encodeURIComponent(nomeCategoria);
    const response = await api.get(`/transacao/categoria/${encoded}/paginado?pagina=${pagina}&tamanho=${tamanho}`);
    return response.data;
  },

  obterPorId: async (id: number): Promise<Transaction> => {
    const response = await api.get<Transaction>(`/transacao/${id}`);
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
