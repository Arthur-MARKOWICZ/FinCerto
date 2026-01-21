import React, { useState } from 'react';
import { ContaCadastroDto, Tipos } from '../types/conta';
import { contaService } from '../services/api';

interface CriarContaModalProps {
  isOpen: boolean;
  onClose: () => void;
  onContaCriada: () => void;
}

const CriarContaModal: React.FC<CriarContaModalProps> = ({ isOpen, onClose, onContaCriada }) => {
  const [formData, setFormData] = useState<ContaCadastroDto>({
    nome: '',
    tipos: Tipos.CORRENTE,
    saldoInicial: 0,
    token: localStorage.getItem('token') || '',
  });
  const [erro, setErro] = useState<string>('');
  const [carregando, setCarregando] = useState<boolean>(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'saldoInicial' ? parseFloat(value) || 0 : value,
      tipos: name === 'tipos' ? value as Tipos : prev.tipos,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    setCarregando(true);

    try {
      await contaService.cadastrar(formData);
      onContaCriada();
      onClose();
      setFormData({
        nome: '',
        tipos: Tipos.CORRENTE,
        saldoInicial: 0,
        token: localStorage.getItem('token') || '',
      });
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

        <form onSubmit={handleSubmit}>
          <div className="space-y-4">
            <div>
              <label htmlFor="nome" className="block text-sm font-medium text-gray-700 mb-1">
                Nome da Conta
              </label>
              <input
                id="nome"
                name="nome"
                type="text"
                required
                value={formData.nome}
                onChange={handleChange}
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
                name="tipos"
                value={formData.tipos}
                onChange={handleChange}
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
                name="saldoInicial"
                type="number"
                step="0.01"
                required
                value={formData.saldoInicial}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="0.00"
              />
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
