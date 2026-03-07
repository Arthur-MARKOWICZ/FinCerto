import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { ContaCadastroDto, Tipos } from '../types/conta';
import { contaService } from '../services/api';
import { useAuth } from '../hooks/useAuth';
import { formatCurrency } from '../utils/format';

interface EditarContaModalProps {
  isOpen: boolean;
  onClose: () => void;
  onContaAtualizada: () => void;
  conta: {
    id: number;
    nome: string;
    tipos: Tipos;
    saldoInicial?: number;
  };
}

const EditarContaModal: React.FC<EditarContaModalProps> = ({ 
  isOpen, 
  onClose, 
  onContaAtualizada,
  conta 
}) => {
  const { register, handleSubmit, reset } = useForm<ContaCadastroDto>();
  const [erro, setErro] = useState<string>('');
  const [carregando, setCarregando] = useState<boolean>(false);
  const { token } = useAuth();

  useEffect(() => {
    if (isOpen && conta) {
      reset({
        nome: conta.nome,
        tipos: conta.tipos,
        saldoInicial: conta.saldoInicial || 0,
      });
    }
  }, [isOpen, conta, reset]);

  const onSubmit = async (data: ContaCadastroDto) => {
    setErro('');
    setCarregando(true);

    try {
      const dadosAtualizacao = {
        ...data,
        saldoInicial: conta.saldoInicial || 0, // Mantém o saldo original
        token: token || ''
      };
      
      await contaService.atualizar(conta.id, dadosAtualizacao);
      onContaAtualizada();
      onClose();
    } catch (error: any) {
      if (error.response?.data?.message) {
        setErro(error.response.data.message);
      } else {
        setErro('Erro ao atualizar conta. Tente novamente mais tarde.');
      }
    } finally {
      setCarregando(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <h2 className="text-xl font-bold mb-4">Editar Conta</h2>
        
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
                {...register('nome', { required: true })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ex: Conta Corrente Itaú"
              />
            </div>

            <div>
              <label htmlFor="tipos" className="block text-sm font-medium text-gray-700 mb-1">
                Tipo de Conta
              </label>
              <select
                id="tipos"
                {...register('tipos', { required: true })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value={Tipos.CORRENTE}>Conta Corrente</option>
                <option value={Tipos.POUPANCA}>Conta Poupança</option>
                <option value={Tipos.CARTAO}>Cartão de Crédito</option>
              </select>
            </div>

            <div className="bg-gray-50 p-3 rounded-md">
              <p className="text-sm text-gray-600">
                <strong>Nota:</strong> O saldo atual da conta será mantido. 
                Para ajustar o saldo, adicione transações de receita ou despesa.
              </p>
              <p className="text-xs text-gray-500 mt-1">
                Saldo atual: {formatCurrency(conta.saldoInicial)}
              </p>
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
              {carregando ? 'Atualizando...' : 'Atualizar Conta'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditarContaModal;
