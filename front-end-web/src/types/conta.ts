export enum Tipos {
  CORRENTE = 'CORRENTE',
  POUPANCA = 'POUPANCA',
  CARTAO = 'CARTAO'
}

export interface ContaCadastroDto {
  nome: string;
  tipos: Tipos;
  saldoInicial: number;
  token: string;
}

export interface Conta {
  id?: number;
  nome: string;
  tipos: Tipos;
  saldo?: number;
  usuarioId?: number;
}
