import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Usuario, LoginRequestDto } from '../types';
import { usuarioService, storageService } from '../services';

interface AuthContextType {
  user: Usuario | null;
  token: string | null;
  isLoading: boolean;
  login: (credentials: LoginRequestDto) => Promise<void>;
  logout: () => Promise<void>;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<Usuario | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    console.log('✅ AuthProvider mounted');
    loadStoredAuth();
  }, []);

  useEffect(() => {
    console.log('🔄 Auth state changed - user:', user, 'token:', token ? 'exists' : 'null');
  }, [user, token]);

  const loadStoredAuth = async () => {
    try {
      console.log('Loading stored auth...');
      const storedToken = await storageService.getToken();
      const storedUser = await storageService.getUser();
      
      console.log('Stored token:', storedToken);
      console.log('Stored user:', storedUser);
      
      if (storedToken && storedUser) {
        setToken(storedToken);
        setUser(storedUser);
        console.log('User authenticated');
      } else {
        console.log('No stored auth found');
      }
    } catch (error) {
      console.error('Error loading stored auth:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (credentials: LoginRequestDto) => {
    try {
      setIsLoading(true);
      const response = await usuarioService.login(credentials);
      
      setToken(response.token);
      await storageService.setToken(response.token);
      
      // TODO: Fetch user details if needed
      // For now, we'll store basic user info from login
      const userData: Usuario = {
        email: credentials.email,
        nome: credentials.email.split('@')[0], // Temporary solution
      };
      
      setUser(userData);
      await storageService.setUser(userData);
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    try {
      console.log('🔴 Starting logout...');
      console.log('Before logout - user:', user, 'token:', token ? 'exists' : 'null');
      
      // Clear storage first
      await storageService.clearAll();
      console.log('✅ Storage cleared');
      
      // Then update state
      setUser(null);
      setToken(null);
      console.log('✅ State updated - user and token set to null');
      console.log('🔴 Logout completed');
    } catch (error) {
      console.error('Logout error:', error);
      // Still clear state even if storage fails
      setUser(null);
      setToken(null);
      throw error;
    }
  };

  const value: AuthContextType = {
    user,
    token,
    isLoading,
    login,
    logout,
    isAuthenticated: !!token && !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
