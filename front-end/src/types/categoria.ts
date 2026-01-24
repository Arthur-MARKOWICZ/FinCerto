export enum Tipo {
  RECEITA = 'RECEITA',
  DESPESA = 'DESPESA'
}

export interface CategoriaCadastroDto {
  nome: string;
  tipo: Tipo;
  token: string;
}

export interface Categoria {
  id?: number;
  nome: string;
  tipo: Tipo;
  usuarioId?: number;
  usuario?: {
    id: number;
    nome: string;
  };
}
