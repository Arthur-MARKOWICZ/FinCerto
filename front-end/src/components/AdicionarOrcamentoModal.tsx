import React, { useState } from 'react';
import { OrcamentoCadastroDto } from '../types/orcamento';
import { orcamentoApiService } from '../services/orcamentoApi';

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
  const [formData, setFormData] = useState<OrcamentoCadastroDto>({
    valorLimite: 0,
    valorInical: 0,
    nome: '',
    prazo: '',
    categoriaNome: '',
    token: ''
  });
  const [carregando, setCarregando] = useState(false);
  const [erro, setErro] = useState('');

  React.useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setFormData(prev => ({ ...prev, token }));
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.nome || !formData.categoriaNome || formData.valorLimite <= 0) {
      setErro('Preencha todos os campos obrigatórios.');
      return;
    }

    try {
      setCarregando(true);
      setErro('');
      
      await orcamentoApiService.cadastrar(formData);
      
      setFormData({
        valorLimite: 0,
        valorInical: 0,
        nome: '',
        prazo: '',
        categoriaNome: '',
        token: formData.token
      });
      
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
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Nome do Orçamento
            </label>
            <input
              type="text"
              value={formData.nome}
              onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Ex: Orçamento Mensal"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Categoria
            </label>
            <input
              type="text"
              value={formData.categoriaNome}
              onChange={(e) => setFormData({ ...formData, categoriaNome: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Ex: Alimentação"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Valor Limite
            </label>
            <input
              type="number"
              value={formData.valorLimite}
              onChange={(e) => setFormData({ ...formData, valorLimite: parseFloat(e.target.value) || 0 })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="0.00"
              step="0.01"
              min="0"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Valor Inicial
            </label>
            <input
              type="number"
              value={formData.valorInical}
              onChange={(e) => setFormData({ ...formData, valorInical: parseFloat(e.target.value) || 0 })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="0.00"
              step="0.01"
              min="0"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Prazo
            </label>
            <input
              type="date"
              value={formData.prazo}
              onChange={(e) => setFormData({ ...formData, prazo: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
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
