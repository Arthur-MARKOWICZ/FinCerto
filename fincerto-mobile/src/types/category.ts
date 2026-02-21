export enum CategoryType {
  RECEITA = 'RECEITA',
  DESPESA = 'DESPESA'
}

export interface CategoriaCadastroDto {
  nome: string;
  tipo: CategoryType;
  token: string;
}

export interface Category {
  id?: number;
  nome: string;
  tipo: CategoryType;
  usuarioId?: number;
  usuario?: {
    id: number;
    nome: string;
  };
}
