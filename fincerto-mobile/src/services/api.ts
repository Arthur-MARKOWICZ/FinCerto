import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { 
  UsuarioCadastroDto, 
  LoginRequestDto, 
  LoginResponseDto, 
  Usuario 
} from '../types/auth';
import { 
  ContaCadastroDto, 
  Account, 
  AccountWithBalance 
} from '../types/account';
import { 
  TransacaoCadastroDto, 
  Transaction 
} from '../types/transaction';
import { 
  CategoriaCadastroDto, 
  Category 
} from '../types/category';

const API_BASE_URL = 'http://localhost:8080/api';
const TOKEN_KEY = '@fincerto_token';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(async (config) => {
  try {
    const token = await AsyncStorage.getItem(TOKEN_KEY);
    console.log('📋 API Interceptor - Token found:', token ? 'yes' : 'no');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('✅ Authorization header added');
    } else {
      console.warn('⚠️ No token found in storage');
    }
  } catch (error) {
    console.error('❌ Error reading token from storage:', error);
  }
  return config;
});

export const usuarioService = {
  cadastrar: async (dados: UsuarioCadastroDto): Promise<Usuario> => {
    try {
      console.log('🔵 usuarioService.cadastrar - Iniciando cadastro');
      console.log('📋 Dados recebidos:', dados);
      console.log('🌐 URL da requisição:', `${API_BASE_URL}/usuarios`);
      
      const response = await api.post<Usuario>('/usuarios', dados);
      
      console.log('✅ Resposta recebida:', response.status);
      console.log('📦 Dados retornados:', response.data);
      
      return response.data;
    } catch (error: any) {
      console.error('❌ Erro no usuarioService.cadastrar:', {
        message: error.message,
        status: error.response?.status,
        data: error.response?.data,
        headers: error.config?.headers
      });
      throw error;
    }
  },

  login: async (dados: LoginRequestDto): Promise<LoginResponseDto> => {
    const response = await api.post<LoginResponseDto>('/usuarios/login', dados);
    return response.data;
  },
};

export const contaService = {
  criar: async (dados: ContaCadastroDto): Promise<Account> => {
    try {
      console.log('🔵 Creating account:', dados);
      const response = await api.post<Account>('/contas', dados);
      console.log('✅ Account created successfully');
      return response.data;
    } catch (error: any) {
      console.error('❌ Error creating account:', {
        status: error.response?.status,
        message: error.response?.data?.message,
        headers: error.config?.headers,
        data: error.response?.data,
      });
      throw error;
    }
  },

  listar: async (): Promise<Account[]> => {
    const token = await AsyncStorage.getItem('token');
    if (!token) {
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

export const transacaoService = {
  criar: async (dados: TransacaoCadastroDto): Promise<Transaction> => {
    const response = await api.post<Transaction>('/transacao', dados);
    return response.data;
  },

  listar: async (): Promise<Transaction[]> => {
    const response = await api.get<{ content: Transaction[] }>('/transacao?pagina=0&tamanho=100');
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

export default api;
