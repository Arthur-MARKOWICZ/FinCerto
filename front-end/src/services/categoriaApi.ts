import axios from 'axios';
import { Categoria, CategoriaCadastroDto } from '../types/categoria';

const API_URL = 'http://localhost:8080/api/categorias';

const categoriaApiService = {
  async listarTodas(): Promise<Categoria[]> {
    const response = await axios.get<Categoria[]>(`${API_URL}/usuario`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
      },
    });
    return response.data;
  },

  async listarPaginado(pagina: number = 0, tamanho: number = 20): Promise<any> {
    const response = await axios.get(`${API_URL}?pagina=${pagina}&tamanho=${tamanho}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
      },
    });
    return response.data;
  },

  async cadastrar(dto: CategoriaCadastroDto): Promise<Categoria> {
    const response = await axios.post<Categoria>(API_URL, dto, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
      },
    });
    return response.data;
  },

  async obterPorUsuario(usuarioId: number): Promise<Categoria[]> {
    // TODO: Implementar quando o back-end tiver endpoint para listar categorias por usuário
    return [];
  },

  async obterPorTipo(tipo: string): Promise<Categoria[]> {
    // TODO: Implementar quando o back-end tiver endpoint para listar categorias por tipo
    return [];
  }
};

export default categoriaApiService;
