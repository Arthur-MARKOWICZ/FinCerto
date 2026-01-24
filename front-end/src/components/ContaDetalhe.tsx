import React, { useState, useEffect } from 'react';
import { Conta, Tipos } from '../types/conta';
import { contaService } from '../services/api';
import AdicionarTransacaoModal from './AdicionarTransacaoModal';
import EditarContaModal from './EditarContaModal';
import TransacoesList from './TransacoesList';
import OrcamentoList from './OrcamentoList';
import SelecionarContaModal from './SelecionarContaModal';

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
    // Não força mais a mudança de tela
    onContaAtualizada?.();
  };

  const handleContaAtualizada = () => {
    onContaAtualizada?.();
  };

  const handleTrocarConta = (novaConta: Conta) => {
    // Fecha o modal primeiro
    setModalSelecionarAberto(false);
    // Delay sincronizado com o SelecionarContaModal (300ms)
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

  const formatarSaldo = (valor: number): string => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="mb-6">
            <button
              onClick={onVoltar}
              className="flex items-center space-x-2 text-gray-600 hover:text-gray-900"
            >
              <span>←</span>
              <span>Voltar para Contas</span>
            </button>
          </div>

          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex justify-between items-start mb-6">
              <div>
                <h1 className="text-2xl font-bold text-gray-900 mb-2">{conta.nome}</h1>
                <div className="flex items-center space-x-4">
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${getTipoCor(conta.tipos)}`}>
                    {getTipoLabel(conta.tipos)}
                  </span>
                  {conta.id && (
                    <span className="text-gray-500 text-sm">ID: #{conta.id}</span>
                  )}
                </div>
              </div>
              <button
                onClick={() => setModalSelecionarAberto(true)}
                className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-md flex items-center space-x-2"
              >
                <span>⚡</span>
                <span>Trocar Conta</span>
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="bg-gray-50 rounded-lg p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Resumo da Conta</h2>
                
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <span className="text-gray-600">Saldo Atual:</span>
                    {carregandoSaldo ? (
                      <div className="text-gray-500">Carregando...</div>
                    ) : (
                      <span className={`text-2xl font-bold ${
                        saldo >= 0 ? 'text-green-600' : 'text-red-600'
                      }`}>
                        {formatarSaldo(saldo)}
                      </span>
                    )}
                  </div>
                  
                  <div className="flex justify-between items-center">
                    <span className="text-gray-600">Tipo:</span>
                    <span className="font-medium">{getTipoLabel(conta.tipos)}</span>
                  </div>
                </div>
              </div>

              <div className="bg-gray-50 rounded-lg p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Ações Rápidas</h2>
                
                <div className="space-y-3">
                  <button
                    onClick={() => setModalTransacaoAberto(true)}
                    className="w-full bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md"
                  >
                    Adicionar Transação
                  </button>
                  <button
                    onClick={() => setModalEditarAberto(true)}
                    className="w-full bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-md"
                  >
                    Editar Conta
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
