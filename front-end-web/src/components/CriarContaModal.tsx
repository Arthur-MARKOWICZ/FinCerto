import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { ContaCadastroDto, Tipos } from '../types/conta';
import { contaService } from '../services/api';
import { useAuth } from '../hooks/useAuth';

interface CriarContaModalProps {
  isOpen: boolean;
  onClose: () => void;
  onContaCriada: () => void;
}

const CriarContaModal: React.FC<CriarContaModalProps> = ({ isOpen, onClose, onContaCriada }) => {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<ContaCadastroDto>({
    defaultValues: {
      tipos: Tipos.CORRENTE,
      saldoInicial: 0
    }
  });
  const { token } = useAuth();
  const [erro, setErro] = useState<string>('');
  const [carregando, setCarregando] = useState<boolean>(false);

  const onSubmit = async (data: ContaCadastroDto) => {
    setErro('');
    setCarregando(true);

    try {
      await contaService.cadastrar({ ...data, token: token || '', saldoInicial: Number(data.saldoInicial) });
      onContaCriada();
      reset();
      onClose();
    } catch (error: any) {
      if (error.response?.data?.message) {
        setErro(error.response.data.message);
      } else {
        setErro('Erro ao criar conta. Tente novamente mais tarde.');
      }
    } finally {
      setCarregando(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <h2 className="text-xl font-bold mb-4">Criar Nova Conta</h2>
        
        {erro && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
            {erro}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-4">
            <div>
              <label htmlFor="nome" className="block text-sm font-medium text-gray-700 mb-1">
                Nome da Conta
              </label>
              <input
                id="nome"
                type="text"
                {...register('nome', { required: 'Nome da conta é obrigatório' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ex: Conta Corrente Itaú"
              />
              {errors.nome && <span className="text-red-500 text-xs">{errors.nome.message as string}</span>}
            </div>

            <div>
              <label htmlFor="tipos" className="block text-sm font-medium text-gray-700 mb-1">
                Tipo de Conta
              </label>
              <select
                id="tipos"
                {...register('tipos', { required: 'Tipo é obrigatório' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value={Tipos.CORRENTE}>Conta Corrente</option>
                <option value={Tipos.POUPANCA}>Conta Poupança</option>
                <option value={Tipos.CARTAO}>Cartão de Crédito</option>
              </select>
            </div>

            <div>
              <label htmlFor="saldoInicial" className="block text-sm font-medium text-gray-700 mb-1">
                Saldo Inicial
              </label>
              <input
                id="saldoInicial"
                type="number"
                step="0.01"
                {...register('saldoInicial', { required: 'Saldo inicial é obrigatório' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.00"
              />
              {errors.saldoInicial && <span className="text-red-500 text-xs">{errors.saldoInicial.message as string}</span>}
            </div>
          </div>

          <div className="flex justify-end space-x-3 mt-6">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={carregando}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {carregando ? 'Criando...' : 'Criar Conta'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CriarContaModal;
