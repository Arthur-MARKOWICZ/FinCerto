export enum TransactionType {
  RECEITA = 'RECEITA',
  DESPESA = 'DESPESA'
}

export interface TransacaoCadastroDto {
  valor: number;
  data: string; 
  descricao: string;
  tipo: TransactionType;
  nomeConta: string;
  nomeCategoria: string;
  token: string;
}

export interface TransactionCategory {
  id: number;
  nome: string;
  tipo: string;
}

export interface TransactionAccount {
  id: number;
  nome: string;
  saldoInicial: number;
  tipos: string;
}

export interface TransactionUser {
  id: number;
  email: string;
  nome: string;
}

export interface Transaction {
  id?: number;
  valor: number;
  date?: string;
  descricao: string;
  tipo: TransactionType;
  categoria?: TransactionCategory;
  conta?: TransactionAccount;
  usuario?: TransactionUser;
}
