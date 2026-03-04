import { CategoriaCadastroDto, Category } from '../types/category';
import api from './api';

export const categoriaService = {
  criar: async (dados: CategoriaCadastroDto): Promise<Category> => {
    const response = await api.post<Category>('/categorias', dados);
    return response.data;
  },

  listar: async (): Promise<Category[]> => {
    const response = await api.get<{ content: Category[] }>('/categorias/usuario?pagina=0&tamanho=100');
    return response.data.content || [];
  },

  atualizar: async (id: number, dados: Partial<Category>): Promise<Category> => {
    const response = await api.put<Category>(`/categorias/${id}`, dados);
    return response.data;
  },

  deletar: async (id: number): Promise<void> => {
    await api.delete(`/categorias/${id}`);
  },
};

export default categoriaService;
