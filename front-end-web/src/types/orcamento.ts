export interface Orcamento {
  id?: number;
  valorLimite: number;
  valorAtual: number;
  nome: string;
  prazo: string;
  categoria: {
    id: number;
    nome: string;
  };
  usuario: {
    id: number;
    nome: string;
  };
}

export interface OrcamentoCadastroDto {
  valorLimite: number;
  valorInical: number;
  nome: string;
  prazo: string;
  categoriaNome: string;
  token: string;
}
