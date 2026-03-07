import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { UsuarioCadastroDto } from '../types/usuario';
import { usuarioService } from '../services/api';

const CadastroForm: React.FC = () => {
  const { register, handleSubmit, formState: { errors } } = useForm<UsuarioCadastroDto>();
  const [erro, setErro] = useState<string>('');
  const [carregando, setCarregando] = useState<boolean>(false);
  const navigate = useNavigate();

  const onSubmit = async (data: UsuarioCadastroDto) => {
    setErro('');
    setCarregando(true);

    try {
      await usuarioService.cadastrar(data);
      navigate('/login');
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
            <Link to="/login" className="font-medium text-blue-600 hover:text-blue-500">
              faça login na sua conta existente
            </Link>
          </p>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
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
                type="text"
                {...register('nome', { required: 'Nome é obrigatório' })}
                className="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                placeholder="Seu nome completo"
              />
              {errors.nome && <span className="text-red-500 text-xs">{errors.nome.message as string}</span>}
            </div>
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                Email
              </label>
              <input
                id="email"
                type="email"
                {...register('email', { required: 'Email é obrigatório' })}
                className="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                placeholder="seu@email.com"
              />
              {errors.email && <span className="text-red-500 text-xs">{errors.email.message as string}</span>}
            </div>
            <div>
              <label htmlFor="senha" className="block text-sm font-medium text-gray-700">
                Senha
              </label>
              <input
                id="senha"
                type="password"
                {...register('senha', { 
                  required: 'Senha é obrigatória',
                  minLength: { value: 6, message: 'No mínimo 6 caracteres' } 
                })}
                className="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                placeholder="Sua senha"
              />
              {errors.senha && <span className="text-red-500 text-xs">{errors.senha.message as string}</span>}
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
