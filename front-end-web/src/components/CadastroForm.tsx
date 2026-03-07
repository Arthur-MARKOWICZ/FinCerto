import React, { useState } from 'react';
import { UsuarioCadastroDto } from '../types/usuario';
import { usuarioService } from '../services/api';

interface CadastroFormProps {
  onSuccess: () => void;
  onLoginClick: () => void;
}

const CadastroForm: React.FC<CadastroFormProps> = ({ onSuccess, onLoginClick }) => {
  const [formData, setFormData] = useState<UsuarioCadastroDto>({
    nome: '',
    email: '',
    senha: '',
  });
  const [erro, setErro] = useState<string>('');
  const [carregando, setCarregando] = useState<boolean>(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    setCarregando(true);

    try {
      await usuarioService.cadastrar(formData);
      onSuccess();
    } catch (error: any) {
      if (error.response?.data?.message) {
        setErro(error.response.data.message);
      } else if (error.response?.status === 400) {
        setErro('Dados inválidos. Verifique as informações e tente novamente.');
      } else {
        setErro('Erro ao cadastrar. Tente novamente mais tarde.');
      }
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Criar nova conta
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Ou{' '}
            <button
              onClick={onLoginClick}
              className="font-medium text-blue-600 hover:text-blue-500"
            >
              faça login na sua conta existente
            </button>
          </p>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {erro && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
              {erro}
            </div>
          )}
          <div className="space-y-4">
            <div>
              <label htmlFor="nome" className="block text-sm font-medium text-gray-700">
                Nome completo
              </label>
              <input
                id="nome"
                name="nome"
                type="text"
                required
                value={formData.nome}
                onChange={handleChange}
                className="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                placeholder="Seu nome completo"
              />
            </div>
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                Email
              </label>
              <input
                id="email"
                name="email"
                type="email"
                required
                value={formData.email}
                onChange={handleChange}
                className="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                placeholder="seu@email.com"
              />
            </div>
            <div>
              <label htmlFor="senha" className="block text-sm font-medium text-gray-700">
                Senha
              </label>
              <input
                id="senha"
                name="senha"
                type="password"
                required
                value={formData.senha}
                onChange={handleChange}
                className="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                placeholder="Sua senha"
              />
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={carregando}
              className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {carregando ? 'Cadastrando...' : 'Cadastrar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CadastroForm;
