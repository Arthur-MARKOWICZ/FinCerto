import React, { useState, useEffect } from 'react';
import { ArrowLeft, Zap, Loader2, PlusCircle, Edit } from 'lucide-react';
import { Conta, Tipos } from '../types/conta';
import { contaService } from '../services/api';
import AdicionarTransacaoModal from './AdicionarTransacaoModal';
import EditarContaModal from './EditarContaModal';
import TransacoesList from './TransacoesList';
import OrcamentoList from './OrcamentoList';
import SelecionarContaModal from './SelecionarContaModal';
import { formatCurrency } from '../utils/format';

interface ContaDetalheProps {
  conta: Conta;
  onVoltar: () => void;
  onContaAtualizada?: () => void;
  onTrocarConta?: () => void;
}

const ContaDetalhe: React.FC<ContaDetalheProps> = ({ 
  conta, 
  onVoltar, 
  onContaAtualizada,
  onTrocarConta
}) => {
  const [saldo, setSaldo] = useState<number>(0);
  const [carregandoSaldo, setCarregandoSaldo] = useState<boolean>(true);
  const [modalTransacaoAberto, setModalTransacaoAberto] = useState<boolean>(false);
  const [modalEditarAberto, setModalEditarAberto] = useState<boolean>(false);
  const [modalSelecionarAberto, setModalSelecionarAberto] = useState<boolean>(false);

  useEffect(() => {
    const carregarSaldo = async () => {
      if (conta.id) {
        try {
          setCarregandoSaldo(true);
          const saldoData = await contaService.obterSaldo(conta.id);
          setSaldo(saldoData);
        } catch (error) {
          console.error('Erro ao carregar saldo:', error);
        } finally {
          setCarregandoSaldo(false);
        }
      }
    };

    carregarSaldo();
  }, [conta.id]);

  const handleTransacaoAdicionada = () => {
    if (conta.id) {
      contaService.obterSaldo(conta.id).then(setSaldo);
    }
    onContaAtualizada?.();
  };

  const handleContaAtualizada = () => {
    onContaAtualizada?.();
  };

  const handleTrocarConta = (novaConta: Conta) => {
    setModalSelecionarAberto(false);
    setTimeout(() => {
      onTrocarConta?.();
    }, 300);
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

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="mb-6">
            <button
              onClick={onVoltar}
              className="flex items-center space-x-2 text-gray-600 hover:text-gray-900 transition-colors"
            >
              <ArrowLeft size={20} />
              <span className="font-medium">Voltar para Contas</span>
            </button>
          </div>

          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <div className="flex justify-between items-start mb-6">
              <div>
                <h1 className="text-2xl font-bold text-gray-900 mb-2">{conta.nome}</h1>
                <div className="flex items-center space-x-4">
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${getTipoCor(conta.tipos)}`}>
                    {getTipoLabel(conta.tipos)}
                  </span>
                  {conta.id && (
                    <span className="text-gray-500 text-sm font-mono bg-gray-50 px-2 py-1 rounded">ID #{conta.id}</span>
                  )}
                </div>
              </div>
              <button
                onClick={() => setModalSelecionarAberto(true)}
                className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2 transition-colors shadow-sm"
              >
                <Zap size={18} />
                <span className="font-medium">Trocar Conta</span>
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="bg-gray-50 rounded-xl p-6 border border-gray-100">
                <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <Zap size={20} className="text-blue-500 mr-2" />
                  Resumo da Conta
                </h2>
                
                <div className="space-y-4">
                  <div className="flex justify-between items-center bg-white p-3 rounded-lg shadow-sm">
                    <span className="text-gray-600 font-medium">Saldo Atual:</span>
                    {carregandoSaldo ? (
                      <Loader2 size={24} className="animate-spin text-blue-500" />
                    ) : (
                      <span className={`text-2xl font-bold ${
                        saldo >= 0 ? 'text-green-600' : 'text-red-600'
                      }`}>
                        {formatCurrency(saldo)}
                      </span>
                    )}
                  </div>
                  
                  <div className="flex justify-between items-center px-2">
                    <span className="text-gray-600 text-sm">Tipo:</span>
                    <span className="font-medium text-gray-800">{getTipoLabel(conta.tipos)}</span>
                  </div>
                </div>
              </div>

              <div className="bg-gray-50 rounded-xl p-6 border border-gray-100">
                <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <Edit size={20} className="text-gray-500 mr-2" />
                  Ações Rápidas
                </h2>
                
                <div className="space-y-3">
                  <button
                    onClick={() => setModalTransacaoAberto(true)}
                    className="w-full bg-green-600 hover:bg-green-700 text-white px-4 py-3 rounded-lg font-medium transition-colors shadow-sm flex justify-center items-center space-x-2"
                  >
                    <PlusCircle size={20} />
                    <span>Adicionar Transação</span>
                  </button>
                  <button
                    onClick={() => setModalEditarAberto(true)}
                    className="w-full bg-gray-600 hover:bg-gray-700 text-white px-4 py-3 rounded-lg font-medium transition-colors shadow-sm flex justify-center items-center space-x-2"
                  >
                    <Edit size={20} />
                    <span>Editar Conta</span>
                  </button>
                </div>
              </div>
            </div>

            <div className="mt-8">
              <TransacoesList 
                contaId={conta.id || 0} 
                contaNome={conta.nome}
                onTransacaoAtualizada={handleTransacaoAdicionada}
              />
            </div>

            <div className="mt-8">
              <OrcamentoList />
            </div>
          </div>
        </div>
      </div>

      <AdicionarTransacaoModal
        isOpen={modalTransacaoAberto}
        onClose={() => setModalTransacaoAberto(false)}
        onTransacaoAdicionada={handleTransacaoAdicionada}
        contaNome={conta.nome}
      />

      <EditarContaModal
        isOpen={modalEditarAberto}
        onClose={() => setModalEditarAberto(false)}
        onContaAtualizada={handleContaAtualizada}
        conta={{
          id: conta.id || 0,
          nome: conta.nome,
          tipos: conta.tipos,
          saldoInicial: conta.saldo
        }}
      />

      <SelecionarContaModal
        isOpen={modalSelecionarAberto}
        onClose={() => setModalSelecionarAberto(false)}
        onContaSelect={handleTrocarConta}
        contaAtualId={conta.id}
      />
    </div>
  );
};

export default ContaDetalhe;
