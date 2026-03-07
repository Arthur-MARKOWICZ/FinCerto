import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { OrcamentoCadastroDto } from '../types/orcamento';
import { orcamentoApiService } from '../services/orcamentoApi';
import { useAuth } from '../hooks/useAuth';

interface AdicionarOrcamentoModalProps {
  isOpen: boolean;
  onClose: () => void;
  onOrcamentoAdicionado?: () => void;
}

const AdicionarOrcamentoModal: React.FC<AdicionarOrcamentoModalProps> = ({
  isOpen,
  onClose,
  onOrcamentoAdicionado
}) => {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<OrcamentoCadastroDto>();
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState('');
  const { token } = useAuth();

  const onSubmit = async (data: OrcamentoCadastroDto) => {
    try {
      setCarregando(true);
      setErro('');
      
      await orcamentoApiService.cadastrar({
        ...data,
        valorLimite: Number(data.valorLimite),
        valorInical: Number(data.valorInical || 0),
        token: token || ''
      });
      
      reset();
      onClose();
      if (onOrcamentoAdicionado) {
        onOrcamentoAdicionado();
      }
    } catch (error: any) {
      setErro('Erro ao criar orçamento. Tente novamente.');
      console.error('Erro:', error);
    } finally {
      setCarregando(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <h2 className="text-xl font-bold mb-4">Adicionar Orçamento</h2>
        
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Nome do Orçamento
            </label>
            <input
              type="text"
              {...register('nome', { required: 'Nome é obrigatório' })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Ex: Orçamento Mensal"
            />
            {errors.nome && <span className="text-red-500 text-xs">{errors.nome.message as string}</span>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Categoria
            </label>
            <input
              type="text"
              {...register('categoriaNome', { required: 'Categoria é obrigatória' })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Ex: Alimentação"
            />
            {errors.categoriaNome && <span className="text-red-500 text-xs">{errors.categoriaNome.message as string}</span>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Valor Limite
            </label>
            <input
              type="number"
              step="0.01"
              min="0"
              {...register('valorLimite', { required: 'Valor limite é obrigatório', min: { value: 0.01, message: 'Valor deve ser maior que 0' } })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="0.00"
            />
            {errors.valorLimite && <span className="text-red-500 text-xs">{errors.valorLimite.message as string}</span>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Valor Inicial
            </label>
            <input
              type="number"
              step="0.01"
              min="0"
              {...register('valorInical')}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="0.00"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Prazo
            </label>
            <input
              type="date"
              {...register('prazo', { required: 'Prazo é obrigatório' })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {errors.prazo && <span className="text-red-500 text-xs">{errors.prazo.message as string}</span>}
          </div>

          {erro && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-3 py-2 rounded">
              {erro}
            </div>
          )}

          <div className="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
              disabled={carregando}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
              disabled={carregando}
            >
              {carregando ? 'Salvando...' : 'Salvar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AdicionarOrcamentoModal;
