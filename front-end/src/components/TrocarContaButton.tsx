import React, { useState } from 'react';
import { Conta } from '../types/conta';
import SelecionarContaModal from './SelecionarContaModal';

interface TrocarContaButtonProps {
  contas: Conta[];
  contaAtual: Conta;
  onContaSelect: (conta: Conta) => void;
}

const TrocarContaButton: React.FC<TrocarContaButtonProps> = ({ 
  contas, 
  contaAtual, 
  onContaSelect 
}) => {
  const [modalAberto, setModalAberto] = useState<boolean>(false);

  const getTipoLabel = (tipo: any): string => {
    switch (tipo) {
      case 'CORRENTE':
        return 'Conta Corrente';
      case 'POUPANCA':
        return 'Conta Poupança';
      case 'CARTAO':
        return 'Cartão de Crédito';
      default:
        return tipo;
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
    <>
      <button
        onClick={() => setModalAberto(true)}
        className="bg-purple-600 hover:bg-purple-700 text-white px-3 py-2 rounded-md flex items-center space-x-2 text-sm"
      >
        <span>⚡</span>
        <span>Trocar Rápido</span>
      </button>

      <SelecionarContaModal
        isOpen={modalAberto}
        onClose={() => setModalAberto(false)}
        onContaSelect={onContaSelect}
        contaAtualId={contaAtual.id}
      />
    </>
  );
};

export default TrocarContaButton;
