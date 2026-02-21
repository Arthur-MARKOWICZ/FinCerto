export enum AccountType {
  CORRENTE = 'CORRENTE',
  POUPANCA = 'POUPANCA',
  CARTAO = 'CARTAO'
}

export interface ContaCadastroDto {
  nome: string;
  tipos: AccountType;
  saldoInicial: number;
  token: string;
}

export interface Account {
  id?: number;
  nome: string;
  tipos: AccountType;
  saldo?: number;
  usuarioId?: number;
  updatedAt?: string;
}

export interface AccountWithBalance extends Account {
  saldoAtual?: number;
}
