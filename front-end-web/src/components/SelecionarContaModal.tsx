import React, { useState, useEffect } from 'react';
import { Loader2, Search, X } from 'lucide-react';
import { Conta, Tipos } from '../types/conta';
import { contaService } from '../services/api';
import { formatCurrency } from '../utils/format';

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

  const contasFiltradas = contas.filter(conta => 
    conta.nome.toLowerCase().includes(filtro.toLowerCase()) ||
    getTipoLabel(conta.tipos).toLowerCase().includes(filtro.toLowerCase())
  );

  const handleContaClick = (conta: Conta) => {
    onClose();
    setTimeout(() => {
      onContaSelect(conta);
    }, 300);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl p-6 w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col shadow-xl">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-bold text-gray-900">Selecionar Conta</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors bg-gray-100 hover:bg-gray-200 rounded-full p-2"
          >
            <X size={20} />
          </button>
        </div>

        <div className="mb-4 relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Search className="h-5 w-5 text-gray-400" />
          </div>
          <input
            type="text"
            placeholder="Buscar contas..."
            value={filtro}
            onChange={(e) => setFiltro(e.target.value)}
            className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 shadow-sm"
          />
        </div>

        {erro && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 shadow-sm">
            {erro}
          </div>
        )}

        <div className="overflow-y-auto flex-1 pr-2 pb-4">
          {carregando ? (
            <div className="flex flex-col justify-center items-center h-48 space-y-3">
              <Loader2 className="animate-spin text-blue-500" size={32} />
              <div className="text-gray-500 font-medium">Carregando contas...</div>
            </div>
          ) : contasFiltradas.length === 0 ? (
            <div className="text-center py-12 border-2 border-dashed border-gray-200 rounded-xl bg-gray-50">
              <div className="text-gray-500 font-medium">
                {filtro ? 'Nenhuma conta encontrada para esta busca.' : 'Você ainda não tem contas cadastradas.'}
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {contasFiltradas.map((conta) => (
                <div
                  key={conta.id}
                  onClick={() => handleContaClick(conta)}
                  className={`bg-white border-2 rounded-xl p-5 cursor-pointer hover:shadow-md transition-all duration-200 ${
                    contaAtualId === conta.id 
                      ? 'border-blue-500 shadow-sm bg-blue-50/10' 
                      : 'border-gray-100 hover:border-blue-300'
                  }`}
                >
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="text-lg font-semibold text-gray-900 truncate pr-2">
                      {conta.nome}
                    </h3>
                    {contaAtualId === conta.id && (
                      <span className="bg-blue-500 text-white text-xs px-2 py-1 rounded-full font-medium shrink-0 shadow-sm">
                        Atual
                      </span>
                    )}
                  </div>
                  
                  <div className="space-y-3">
                    <div className="flex justify-between items-center text-sm">
                      <span className="text-gray-500">Tipo:</span>
                      <span className={`px-2 py-1 rounded-md text-xs font-bold ${getTipoCor(conta.tipos)}`}>
                        {getTipoLabel(conta.tipos)}
                      </span>
                    </div>
                    
                    <div className="flex justify-between items-center text-sm">
                      <span className="text-gray-500">Saldo:</span>
                      <span className={`text-base font-bold ${
                        (conta.saldo || 0) >= 0 ? 'text-green-600' : 'text-red-600'
                      }`}>
                        {formatCurrency(conta.saldo)}
                      </span>
                    </div>
                    
                    {conta.id && (
                      <div className="flex justify-between items-center text-sm">
                        <span className="text-gray-500">ID:</span>
                        <span className="text-xs text-gray-400 font-mono bg-gray-50 px-2 py-1 rounded">#{conta.id}</span>
                      </div>
                    )}
                  </div>

                  <div className="mt-4 pt-4 border-t border-gray-100">
                    <div className="text-sm text-blue-600 font-medium text-center group-hover:text-blue-700">
                      {contaAtualId === conta.id ? 'Conta atual' : 'Clique para selecionar →'}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="flex justify-end mt-4 pt-4 border-t border-gray-100 px-2">
          <button
            onClick={onClose}
            className="px-6 py-2.5 text-gray-700 font-medium bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors shadow-sm"
          >
            Cancelar
          </button>
        </div>
      </div>
    </div>
  );
};

export default SelecionarContaModal;
