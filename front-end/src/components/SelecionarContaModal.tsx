import React, { useState, useEffect } from 'react';
import { Conta, Tipos } from '../types/conta';
import { contaService } from '../services/api';

interface SelecionarContaModalProps {
  isOpen: boolean;
  onClose: () => void;
  onContaSelect: (conta: Conta) => void;
  contaAtualId?: number;
}

const SelecionarContaModal: React.FC<SelecionarContaModalProps> = ({ 
  isOpen, 
  onClose, 
  onContaSelect,
  contaAtualId 
}) => {
  const [contas, setContas] = useState<Conta[]>([]);
  const [carregando, setCarregando] = useState<boolean>(true);
  const [erro, setErro] = useState<string>('');
  const [filtro, setFiltro] = useState<string>('');

  useEffect(() => {
    if (isOpen) {
      carregarContas();
    }
  }, [isOpen]);

  const carregarContas = async () => {
    try {
      setCarregando(true);
      const token = localStorage.getItem('token');
      if (!token) {
        setErro('Token não encontrado');
        return;
      }
      
      const response = await contaService.obterPorUsuario(token);
      const contasComSaldos = await Promise.all(
        (response.content || []).map(async (conta) => {
          if (conta.id) {
            try {
              const saldo = await contaService.obterSaldo(conta.id);
              return { ...conta, saldo };
            } catch (error) {
              return { ...conta, saldo: conta.saldo || 0 };
            }
          }
          return { ...conta, saldo: conta.saldo || 0 };
        })
      );
      setContas(contasComSaldos);
    } catch (error: any) {
      setErro('Erro ao carregar contas. Tente novamente mais tarde.');
    } finally {
      setCarregando(false);
    }
  };

  const getTipoLabel = (tipo: Tipos): string => {
    switch (tipo) {
      case Tipos.CORRENTE:
        return 'Conta Corrente';
      case Tipos.POUPANCA:
        return 'Conta Poupança';
      case Tipos.CARTAO:
        return 'Cartão de Crédito';
      default:
        return tipo;
    }
  };

  const getTipoCor = (tipo: Tipos): string => {
    switch (tipo) {
      case Tipos.CORRENTE:
        return 'bg-blue-100 text-blue-800';
      case Tipos.POUPANCA:
        return 'bg-green-100 text-green-800';
      case Tipos.CARTAO:
        return 'bg-purple-100 text-purple-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatarSaldo = (saldo: number | undefined): string => {
    if (saldo === undefined) return 'R$ 0,00';
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(saldo);
  };

  const contasFiltradas = contas.filter(conta => 
    conta.nome.toLowerCase().includes(filtro.toLowerCase()) ||
    getTipoLabel(conta.tipos).toLowerCase().includes(filtro.toLowerCase())
  );

  const handleContaClick = (conta: Conta) => {
    // Fecha o modal imediatamente
    onClose();
    // Delay para garantir fechamento completo antes da navegação
    setTimeout(() => {
      onContaSelect(conta);
    }, 300);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-4xl max-h-[90vh] overflow-hidden">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-bold text-gray-900">Selecionar Conta</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 text-2xl leading-none"
          >
            ×
          </button>
        </div>

        <div className="mb-4">
          <input
            type="text"
            placeholder="Buscar contas..."
            value={filtro}
            onChange={(e) => setFiltro(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {erro && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
            {erro}
          </div>
        )}

        {carregando ? (
          <div className="flex justify-center items-center h-64">
            <div className="text-gray-500">Carregando contas...</div>
          </div>
        ) : contasFiltradas.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-gray-500">
              {filtro ? 'Nenhuma conta encontrada para esta busca.' : 'Você ainda não tem contas cadastradas.'}
            </div>
          </div>
        ) : (
          <div className="overflow-y-auto max-h-96">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {contasFiltradas.map((conta) => (
                <div
                  key={conta.id}
                  onClick={() => handleContaClick(conta)}
                  className={`bg-white border-2 rounded-lg p-4 cursor-pointer hover:shadow-lg transition-all duration-200 ${
                    contaAtualId === conta.id 
                      ? 'border-blue-500 shadow-lg' 
                      : 'border-gray-200 hover:border-blue-300'
                  }`}
                >
                  <div className="flex justify-between items-start mb-3">
                    <h3 className="text-lg font-semibold text-gray-900 truncate">
                      {conta.nome}
                    </h3>
                    {contaAtualId === conta.id && (
                      <span className="bg-blue-500 text-white text-xs px-2 py-1 rounded-full">
                        Atual
                      </span>
                    )}
                  </div>
                  
                  <div className="space-y-2">
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">Tipo:</span>
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${getTipoCor(conta.tipos)}`}>
                        {getTipoLabel(conta.tipos)}
                      </span>
                    </div>
                    
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">Saldo:</span>
                      <span className={`text-sm font-bold ${
                        (conta.saldo || 0) >= 0 ? 'text-green-600' : 'text-red-600'
                      }`}>
                        {formatarSaldo(conta.saldo)}
                      </span>
                    </div>
                    
                    {conta.id && (
                      <div className="flex justify-between items-center">
                        <span className="text-sm text-gray-600">ID:</span>
                        <span className="text-xs text-gray-500">#{conta.id}</span>
                      </div>
                    )}
                  </div>

                  <div className="mt-3 pt-3 border-t border-gray-200">
                    <div className="text-xs text-blue-600 font-medium text-center">
                      {contaAtualId === conta.id ? 'Conta atual' : 'Clique para selecionar →'}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="flex justify-end mt-6 pt-4 border-t border-gray-200">
          <button
            onClick={onClose}
            className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
          >
            Cancelar
          </button>
        </div>
      </div>
    </div>
  );
};

export default SelecionarContaModal;
