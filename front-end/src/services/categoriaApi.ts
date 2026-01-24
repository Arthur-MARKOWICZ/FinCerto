import axios from 'axios';
import { Categoria, CategoriaCadastroDto } from '../types/categoria';

const API_URL = 'http://localhost:8080/api/categorias';

const categoriaApiService = {
  async obterPorUsuario(pagina: number = 0, tamanho: number = 50): Promise<any> {
    console.log(`Carregando categorias - página: ${pagina}, tamanho: ${tamanho}`);
    const response = await axios.get(`${API_URL}/usuario?pagina=${pagina}&tamanho=${tamanho}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
      },
    });
    console.log('Resposta da API categorias:', response.data);
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
};

export default categoriaApiService;
