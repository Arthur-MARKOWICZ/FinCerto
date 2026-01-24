import React, { useState, useEffect } from 'react';
import { Conta, Tipos } from '../types/conta';
import { contaService } from '../services/api';
import CriarContaModal from './CriarContaModal';
import SelecionarContaModal from './SelecionarContaModal';

interface ContasDashboardProps {
  onContaSelect: (conta: Conta) => void;
  contaAtualId?: number;
}

interface ContaComSaldo extends Conta {
  saldoAtual?: number;
}

const ContasDashboard: React.FC<ContasDashboardProps> = ({ 
  onContaSelect, 
  contaAtualId 
}) => {
  const [contas, setContas] = useState<ContaComSaldo[]>([]);
  const [carregando, setCarregando] = useState<boolean>(true);
  const [erro, setErro] = useState<string>('');
  const [modalCriarAberto, setModalCriarAberto] = useState<boolean>(false);
  const [modalSelecionarAberto, setModalSelecionarAberto] = useState<boolean>(false);

  const carregarSaldos = async (contasBase: Conta[]): Promise<ContaComSaldo[]> => {
    const contasComSaldos = await Promise.all(
      contasBase.map(async (conta) => {
        if (conta.id) {
          try {
            const saldo = await contaService.obterSaldo(conta.id);
            return { ...conta, saldoAtual: saldo };
          } catch (error) {
            console.error(`Erro ao carregar saldo da conta ${conta.id}:`, error);
            return { ...conta, saldoAtual: conta.saldo || 0 };
          }
        }
        return { ...conta, saldoAtual: conta.saldo || 0 };
      })
    );
    return contasComSaldos;
  };

  const carregarContas = async () => {
    try {
      setCarregando(true);
      const token = localStorage.getItem('token');
      if (!token) {
        setErro('Token não encontrado');
        return;
      }
      
      const response = await contaService.obterPorUsuario(token);
      const contasBase = response.content || [];
      const contasComSaldos = await carregarSaldos(contasBase);
      setContas(contasComSaldos);
    } catch (error: any) {
      setErro('Erro ao carregar contas. Tente novamente mais tarde.');
    } finally {
      setCarregando(false);
    }
  };

  useEffect(() => {
    carregarContas();
  }, []);

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

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold text-gray-900">Minhas Contas</h1>
            <div className="flex space-x-3">
              <button
                onClick={() => setModalSelecionarAberto(true)}
                className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-md flex items-center space-x-2"
              >
                <span className="text-lg">⚡</span>
                <span>Trocar Conta</span>
              </button>
              <button
                onClick={() => setModalCriarAberto(true)}
                className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md flex items-center space-x-2"
              >
                <span className="text-lg">+</span>
                <span>Nova Conta</span>
              </button>
            </div>
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
          ) : contas.length === 0 ? (
            <div className="text-center py-12">
              <div className="text-gray-500 text-lg mb-4">Você ainda não tem contas cadastradas</div>
              <button
                onClick={() => setModalCriarAberto(true)}
                className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-md"
              >
                Criar Primeira Conta
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {contas.map((conta) => (
                <div
                  key={conta.id}
                  onClick={() => onContaSelect(conta)}
                  className={`bg-white rounded-lg shadow-md p-6 cursor-pointer hover:shadow-lg transition-shadow duration-200 border-2 ${
                    contaAtualId === conta.id ? 'border-blue-500' : 'border-transparent'
                  }`}
                >
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="text-lg font-semibold text-gray-900">{conta.nome}</h3>
                    <div className="flex items-center space-x-2">
                      {contaAtualId === conta.id && (
                        <span className="bg-blue-500 text-white text-xs px-2 py-1 rounded-full">
                          Atual
                        </span>
                      )}
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${getTipoCor(conta.tipos)}`}>
                        {getTipoLabel(conta.tipos)}
                      </span>
                    </div>
                  </div>
                  
                  <div className="space-y-2">
                    <div className="flex justify-between items-center">
                      <span className="text-gray-600">Saldo:</span>
                      <span className={`text-lg font-bold ${
                        (conta.saldoAtual || 0) >= 0 ? 'text-green-600' : 'text-red-600'
                      }`}>
                        {formatarSaldo(conta.saldoAtual)}
                      </span>
                    </div>
                    
                    {conta.id && (
                      <div className="flex justify-between items-center">
                        <span className="text-gray-600">ID:</span>
                        <span className="text-gray-500 text-sm">#{conta.id}</span>
                      </div>
                    )}
                  </div>

                  <div className="mt-4 pt-4 border-t border-gray-200">
                    <div className="text-sm text-blue-600 font-medium text-center">
                      {contaAtualId === conta.id ? 'Conta atual' : 'Clique para ver detalhes →'}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <CriarContaModal
        isOpen={modalCriarAberto}
        onClose={() => setModalCriarAberto(false)}
        onContaCriada={carregarContas}
      />

      <SelecionarContaModal
        isOpen={modalSelecionarAberto}
        onClose={() => setModalSelecionarAberto(false)}
        onContaSelect={onContaSelect}
        contaAtualId={contaAtualId}
      />
    </div>
  );
};

export default ContasDashboard;
