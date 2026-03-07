import axios from 'axios';
import { Alert, Platform } from 'react-native';
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



// Default fallback for device testing, otherwise localhost
// Use your machine's IP here if testing on physical device (e.g. 192.168.0.x)
const API_BASE_URL = Platform.OS === 'android' ? 'http://10.0.2.2:8080/api' : 'http://localhost:8080/api';
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

api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    if (!error.response) {
      // Netowrk error or server offline
      Alert.alert(
        'Erro de Conexão',
        'Não foi possível conectar ao servidor. Verifique sua internet ou tente novamente mais tarde.'
      );
    } else if (error.response.status === 401) {
      // Unauthorized / Token expired
      const token = await AsyncStorage.getItem(TOKEN_KEY);
      if (token) {
        Alert.alert(
          'Sessão Expirada',
          'Sua sessão expirou por segurança. Por favor, faça login novamente.'
        );
        await AsyncStorage.removeItem(TOKEN_KEY);
        // It's ideal to trigger a logout in contexts, but Alert + token removal handles the basic safety
      }
    }
    return Promise.reject(error);
  }
);

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
export default api;
