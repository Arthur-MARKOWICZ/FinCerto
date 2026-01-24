export enum TransacaoTipos {
  RECEITA = 'RECEITA',
  DESPESA = 'DESPESA'
}

export interface TransacaoCadastroDto {
  valor: number;
  data: string; // Formato ISO string para LocalDateTime do back-end
  descricao: string;
  tipo: TransacaoTipos;
  nomeConta: string;
  nomeCategoria: string;
  token: string;
}

export interface Categoria {
  id: number;
  nome: string;
  tipo: string;
}

export interface Conta {
  id: number;
  nome: string;
  saldoInicial: number;
  tipos: string;
}

export interface Usuario {
  id: number;
  email: string;
  nome: string;
}

export interface Transacao {
  id?: number;
  valor: number;
  date?: string;
  descricao: string;
  tipo: TransacaoTipos;
  categoria?: Categoria;
  conta?: Conta;
  usuario?: Usuario;
}
